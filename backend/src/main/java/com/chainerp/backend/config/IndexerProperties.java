package com.chainerp.backend.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indexer")
public class IndexerProperties {
    private String rpcUrl;
    private long startBlock;
    private long confirmations = 3;
    private long chunkSize = 2_000;
    private Map<String, String> contracts = new LinkedHashMap<>();

    public List<ContractConfig> configuredContracts() {
        List<ContractConfig> configured = new ArrayList<>();
        contracts.forEach((name, address) -> {
            if (address != null && !address.isBlank()) {
                if (!address.matches("0x[0-9a-fA-F]{40}")) {
                    throw new IllegalStateException("Invalid address configured for contract: " + name);
                }
                configured.add(new ContractConfig(name, address.toLowerCase()));
            }
        });
        if (configured.isEmpty()) {
            throw new IllegalStateException("Configure at least one address under indexer.contracts");
        }
        return configured;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public long getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(long startBlock) {
        this.startBlock = startBlock;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Map<String, String> getContracts() {
        return contracts;
    }

    public void setContracts(Map<String, String> contracts) {
        this.contracts = contracts;
    }

    public record ContractConfig(String name, String address) {
    }
}
