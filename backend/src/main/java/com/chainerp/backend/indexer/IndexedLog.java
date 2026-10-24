package com.chainerp.backend.indexer;

public record IndexedLog(
    String eventName,
    String transactionHash,
    int logIndex,
    long blockNumber,
    String blockHash,
    String payloadJson
) {
}
