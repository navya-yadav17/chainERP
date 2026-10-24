# chainERP event indexer

This Spring Boot service indexes EVM logs emitted by configured chainERP contracts into PostgreSQL and exposes them through the event-query API.

## Requirements

- Java 17
- Maven 3.8+
- PostgreSQL 13+
- An Ethereum-compatible JSON-RPC endpoint

## Configuration

Set these environment variables before starting the service:

- `INDEXER_RPC_URL` — read-only JSON-RPC endpoint
- `TREASURY_CONTRACT_ADDRESS`, `PAYROLL_CONTRACT_ADDRESS`, and/or `SUBSCRIPTION_MANAGER_CONTRACT_ADDRESS` — deployed contract addresses; at least one is required
- `INDEXER_START_BLOCK` — first block to scan (defaults to `0`)
- `INDEXER_CONFIRMATIONS` — confirmation depth before indexing (defaults to `3`)
- `INDEXER_CHUNK_SIZE` — maximum blocks per RPC query (defaults to `2000`)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` — PostgreSQL connection (defaults target a local development database)

Flyway creates the event, cursor, and checkpoint tables at startup. The service stores raw event topics and data in `payload_json`, preserving unknown event types as well as the currently documented chainERP events. Confirmed block checkpoints are retained so the indexer can rewind to the last canonical checkpoint after a chain reorganization.

## Run

From this directory:

```sh
mvn spring-boot:run
mvn test
```

Query indexed events with `GET /api/v1/events/contracts/{contract}/events?page=0&size=50`. Contract names match their configured keys, such as `treasury`. Page size is limited to 100.

The API is an initial development endpoint and does not yet implement the JWT/RBAC policy described in the API design. Protect it before exposing the service outside a trusted environment.
