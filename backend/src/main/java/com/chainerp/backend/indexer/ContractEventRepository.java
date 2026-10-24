package com.chainerp.backend.indexer;

import java.util.List;
import java.util.Optional;

import com.chainerp.backend.config.IndexerProperties.ContractConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ContractEventRepository {
    private static final RowMapper<IndexCursor> CURSOR_MAPPER = (result, rowNumber) ->
        new IndexCursor(result.getLong("last_processed_block"), result.getString("last_processed_block_hash"));

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RowMapper<IndexedContractEvent> eventMapper;

    public ContractEventRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.objectMapper = objectMapper;
        this.eventMapper = (result, rowNumber) -> new IndexedContractEvent(
            result.getLong("id"),
            result.getLong("chain_id"),
            result.getString("contract_name"),
            result.getString("contract_address"),
            result.getString("event_name"),
            result.getString("transaction_hash"),
            result.getInt("log_index"),
            result.getLong("block_number"),
            result.getString("block_hash"),
            result.getTimestamp("indexed_at").toInstant().toString(),
            readPayloadJson(result.getString("payload_json"))
        );
    }

    public Optional<IndexCursor> findCursor(long chainId, String contractAddress) {
        List<IndexCursor> cursors = jdbcTemplate.query(
            """
                SELECT last_processed_block, last_processed_block_hash
                FROM indexer_cursors
                WHERE chain_id = ? AND contract_address = ?
                """,
            CURSOR_MAPPER,
            chainId,
            contractAddress
        );
        return cursors.stream().findFirst();
    }

    public Optional<IndexCursor> findPreviousCheckpoint(long chainId, String contractAddress, long beforeBlock) {
        List<IndexCursor> checkpoints = jdbcTemplate.query(
            """
                SELECT block_number AS last_processed_block, block_hash AS last_processed_block_hash
                FROM indexer_checkpoints
                WHERE chain_id = ? AND contract_address = ? AND block_number < ?
                ORDER BY block_number DESC
                LIMIT 1
                """,
            CURSOR_MAPPER,
            chainId,
            contractAddress,
            beforeBlock
        );
        return checkpoints.stream().findFirst();
    }

    @Transactional
    public void saveRange(
        long chainId,
        ContractConfig contract,
        long endBlock,
        String endBlockHash,
        List<IndexedLog> logs
    ) {
        jdbcTemplate.update(
            """
                INSERT INTO indexer_checkpoints (chain_id, contract_address, block_number, block_hash)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (chain_id, contract_address, block_number) DO UPDATE
                SET block_hash = EXCLUDED.block_hash
                """,
            chainId,
            contract.address(),
            endBlock,
            endBlockHash
        );

        for (IndexedLog log : logs) {
            jdbcTemplate.update(
                """
                    INSERT INTO contract_events (
                        chain_id, contract_name, contract_address, event_name,
                        transaction_hash, log_index, block_number, block_hash, payload_json
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb))
                    ON CONFLICT (chain_id, contract_address, transaction_hash, log_index) DO NOTHING
                    """,
                chainId,
                contract.name(),
                contract.address(),
                log.eventName(),
                log.transactionHash(),
                log.logIndex(),
                log.blockNumber(),
                log.blockHash(),
                log.payloadJson()
            );
        }

        jdbcTemplate.update(
            """
                INSERT INTO indexer_cursors (chain_id, contract_name, contract_address, last_processed_block, last_processed_block_hash)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (chain_id, contract_address) DO UPDATE
                SET contract_name = EXCLUDED.contract_name,
                    last_processed_block = EXCLUDED.last_processed_block,
                    last_processed_block_hash = EXCLUDED.last_processed_block_hash,
                    updated_at = now()
                """,
            chainId,
            contract.name(),
            contract.address(),
            endBlock,
            endBlockHash
        );
    }

    @Transactional
    public void rewind(
        long chainId,
        ContractConfig contract,
        long rewindToBlock,
        String rewindToBlockHash
    ) {
        jdbcTemplate.update(
            """
                DELETE FROM contract_events
                WHERE chain_id = ? AND contract_address = ? AND block_number > ?
                """,
            chainId,
            contract.address(),
            rewindToBlock
        );
        jdbcTemplate.update(
            """
                DELETE FROM indexer_checkpoints
                WHERE chain_id = ? AND contract_address = ? AND block_number > ?
                """,
            chainId,
            contract.address(),
            rewindToBlock
        );
        jdbcTemplate.update(
            """
                UPDATE indexer_cursors
                SET last_processed_block = ?, last_processed_block_hash = ?, updated_at = now()
                WHERE chain_id = ? AND contract_address = ?
                """,
            rewindToBlock,
            rewindToBlockHash,
            chainId,
            contract.address()
        );
    }

    public EventPage findEvents(String contractName, int page, int size) {
        long offset = (long) page * size;
        List<IndexedContractEvent> events = namedJdbcTemplate.query(
            """
                SELECT id, chain_id, contract_name, contract_address, event_name,
                       transaction_hash, log_index, block_number, block_hash, indexed_at,
                       payload_json::text AS payload_json
                FROM contract_events
                WHERE contract_name = :contractName
                ORDER BY block_number DESC, log_index DESC
                LIMIT :size OFFSET :offset
                """,
            new MapSqlParameterSource()
                .addValue("contractName", contractName)
                .addValue("size", size)
                .addValue("offset", offset),
            eventMapper
        );
        Long total = namedJdbcTemplate.queryForObject(
            "SELECT count(*) FROM contract_events WHERE contract_name = :contractName",
            new MapSqlParameterSource("contractName", contractName),
            Long.class
        );
        return new EventPage(events, page, size, total == null ? 0 : total);
    }

    private JsonNode readPayloadJson(String payloadJson) {
        try {
            return objectMapper.readTree(payloadJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored event payload is not valid JSON", exception);
        }
    }

    public record IndexedContractEvent(
        long id,
        long chainId,
        String contractName,
        String contractAddress,
        String eventName,
        String transactionHash,
        int logIndex,
        long blockNumber,
        String blockHash,
        String indexedAt,
        JsonNode payloadJson
    ) {
    }

    public record EventPage(
        List<IndexedContractEvent> events,
        int page,
        int size,
        long totalElements
    ) {
    }
}
