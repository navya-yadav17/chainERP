package com.chainerp.backend.indexer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.chainerp.backend.config.IndexerProperties;
import com.chainerp.backend.config.IndexerProperties.ContractConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

@Service
public class ContractEventIndexer {
    private final Web3j web3j;
    private final IndexerProperties properties;
    private final ContractEventRepository repository;
    private final ObjectMapper objectMapper;

    public ContractEventIndexer(
        Web3j web3j,
        IndexerProperties properties,
        ContractEventRepository repository,
        ObjectMapper objectMapper
    ) {
        this.web3j = web3j;
        this.properties = properties;
        this.repository = repository;
        this.objectMapper = objectMapper;
        validateConfiguration();
    }

    @Scheduled(fixedDelayString = "${indexer.poll-interval-ms:5000}")
    public void indexNewEvents() throws IOException {
        var chainResponse = web3j.ethChainId().send();
        if (chainResponse.hasError() || chainResponse.getChainId() == null) {
            throw new IOException("eth_chainId failed: " + rpcError(chainResponse.getError()));
        }
        var blockResponse = web3j.ethBlockNumber().send();
        if (blockResponse.hasError() || blockResponse.getBlockNumber() == null) {
            throw new IOException("eth_blockNumber failed: " + rpcError(blockResponse.getError()));
        }
        long chainId = chainResponse.getChainId().longValueExact();
        long latestBlock = blockResponse.getBlockNumber().longValueExact();
        long finalizedBlock = latestBlock - properties.getConfirmations();
        if (finalizedBlock < properties.getStartBlock()) {
            return;
        }

        for (ContractConfig contract : properties.configuredContracts()) {
            indexContract(chainId, contract, finalizedBlock);
        }
    }

    private void indexContract(long chainId, ContractConfig contract, long finalizedBlock) throws IOException {
        Optional<IndexCursor> existingCursor = repository.findCursor(chainId, contract.address());
        long lastProcessedBlock = existingCursor
            .map(IndexCursor::blockNumber)
            .orElse(properties.getStartBlock() - 1);
        boolean cursorRewound = false;

        if (lastProcessedBlock > finalizedBlock) {
            repository.rewind(chainId, contract, finalizedBlock, blockHash(finalizedBlock));
            lastProcessedBlock = finalizedBlock;
            cursorRewound = true;
        }

        if (existingCursor.isPresent() && !cursorRewound && lastProcessedBlock >= properties.getStartBlock()) {
            String canonicalHash = blockHash(lastProcessedBlock);
            if (!canonicalHash.equalsIgnoreCase(existingCursor.get().blockHash())) {
                Optional<IndexCursor> commonCheckpoint =
                    repository.findPreviousCheckpoint(chainId, contract.address(), lastProcessedBlock);
                while (commonCheckpoint.isPresent()
                    && !blockHash(commonCheckpoint.get().blockNumber())
                        .equalsIgnoreCase(commonCheckpoint.get().blockHash())) {
                    commonCheckpoint = repository.findPreviousCheckpoint(
                        chainId,
                        contract.address(),
                        commonCheckpoint.get().blockNumber()
                    );
                }
                long rewindToBlock = commonCheckpoint
                    .map(IndexCursor::blockNumber)
                    .orElse(properties.getStartBlock() - 1);
                String rewindHash = commonCheckpoint.map(IndexCursor::blockHash).orElse(null);
                repository.rewind(chainId, contract, rewindToBlock, rewindHash);
                lastProcessedBlock = rewindToBlock;
            }
        }

        long nextBlock = Math.max(properties.getStartBlock(), lastProcessedBlock + 1);
        while (nextBlock <= finalizedBlock) {
            long endBlock = nextBlock + Math.min(properties.getChunkSize() - 1, finalizedBlock - nextBlock);
            List<IndexedLog> logs = fetchLogs(contract, nextBlock, endBlock);
            repository.saveRange(chainId, contract, endBlock, blockHash(endBlock), logs);
            nextBlock = endBlock + 1;
        }
    }

    private List<IndexedLog> fetchLogs(ContractConfig contract, long fromBlock, long toBlock) throws IOException {
        EthFilter filter = new EthFilter(
            new DefaultBlockParameterNumber(BigInteger.valueOf(fromBlock)),
            new DefaultBlockParameterNumber(BigInteger.valueOf(toBlock)),
            contract.address()
        );
        var response = web3j.ethGetLogs(filter).send();
        if (response.hasError()) {
            throw new IOException("eth_getLogs failed: " + rpcError(response.getError()));
        }
        List<EthLog.LogResult<?>> results = response.getLogs();
        if (results == null) {
            throw new IOException("eth_getLogs returned no result");
        }
        List<IndexedLog> logs = new ArrayList<>(results.size());
        for (EthLog.LogResult<?> result : results) {
            Log log = (Log) result.get();
            if (log.getTopics() == null || log.getTopics().isEmpty()) {
                continue;
            }
            logs.add(new IndexedLog(
                KnownContractEvents.nameForTopic(log.getTopics().get(0)),
                log.getTransactionHash(),
                log.getLogIndex().intValueExact(),
                log.getBlockNumber().longValueExact(),
                log.getBlockHash(),
                payloadJson(log)
            ));
        }
        return logs;
    }

    private String blockHash(long blockNumber) throws IOException {
        var response = web3j.ethGetBlockByNumber(
            new DefaultBlockParameterNumber(BigInteger.valueOf(blockNumber)),
            false
        ).send();
        if (response.hasError()) {
            throw new IOException("eth_getBlockByNumber failed: " + rpcError(response.getError()));
        }
        EthBlock.Block block = response.getBlock();
        if (block == null || block.getHash() == null) {
            throw new IOException("RPC did not return block " + blockNumber);
        }
        return block.getHash();
    }

    private String rpcError(org.web3j.protocol.core.Response.Error error) {
        return error == null ? "empty RPC response" : error.getMessage();
    }

    private String payloadJson(Log log) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("topics", log.getTopics());
        payload.put("data", log.getData());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize log payload", exception);
        }
    }

    private void validateConfiguration() {
        if (properties.getStartBlock() < 0) {
            throw new IllegalStateException("indexer.start-block cannot be negative");
        }
        if (properties.getConfirmations() < 0 || properties.getChunkSize() < 1) {
            throw new IllegalStateException("Indexer confirmations and chunk size are invalid");
        }
        properties.configuredContracts();
    }
}
