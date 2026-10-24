CREATE TABLE contract_events (
    id BIGSERIAL PRIMARY KEY,
    chain_id BIGINT NOT NULL,
    contract_name VARCHAR(100) NOT NULL,
    contract_address VARCHAR(42) NOT NULL,
    event_name VARCHAR(150) NOT NULL,
    transaction_hash VARCHAR(66) NOT NULL,
    log_index INTEGER NOT NULL,
    block_number BIGINT NOT NULL,
    block_hash VARCHAR(66) NOT NULL,
    indexed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload_json JSONB NOT NULL,
    CONSTRAINT uq_contract_events_log UNIQUE (chain_id, contract_address, transaction_hash, log_index)
);

CREATE INDEX idx_contract_events_query
    ON contract_events (contract_name, block_number DESC, log_index DESC);

CREATE TABLE indexer_cursors (
    chain_id BIGINT NOT NULL,
    contract_name VARCHAR(100) NOT NULL,
    contract_address VARCHAR(42) NOT NULL,
    last_processed_block BIGINT NOT NULL,
    last_processed_block_hash VARCHAR(66),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (chain_id, contract_address)
);

CREATE TABLE indexer_checkpoints (
    chain_id BIGINT NOT NULL,
    contract_address VARCHAR(42) NOT NULL,
    block_number BIGINT NOT NULL,
    block_hash VARCHAR(66) NOT NULL,
    PRIMARY KEY (chain_id, contract_address, block_number)
);
