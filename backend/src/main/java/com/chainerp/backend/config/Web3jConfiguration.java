package com.chainerp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfiguration {
    @Bean(destroyMethod = "shutdown")
    public Web3j web3j(IndexerProperties properties) {
        String rpcUrl = properties.getRpcUrl();
        if (rpcUrl == null || rpcUrl.isBlank()) {
            throw new IllegalStateException("Set INDEXER_RPC_URL to an Ethereum-compatible JSON-RPC endpoint");
        }
        return Web3j.build(new HttpService(rpcUrl));
    }
}
