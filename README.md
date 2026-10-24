# chainERP

chainERP is an early-stage blockchain-enabled ERP project focused on auditable financial and operational events. Its intended modules include treasury, payroll, subscriptions, and integrations for supply-chain reconciliation.

## Project status

This repository currently contains:

- **Solidity contracts** for treasury transfers, payroll-run events, and subscription-renewal events in [`contracts/`](./contracts/).
- **A Spring Boot event indexer** in [`backend/`](./backend/) that polls configured EVM contracts, stores confirmed events in PostgreSQL, and exposes a paginated events endpoint.
- **Architecture, requirements, API, database, and roadmap notes** in [`docs/`](./docs/).

The project is not yet a complete ERP application. The React frontend, business-domain backend services, JWT/RBAC, production deployment, and end-to-end integration are not implemented. The event API currently has no authentication and should only be run in a trusted development environment.

## Requirements

- Java 17 and Maven 3.8+ for the backend.
- PostgreSQL 13+ for indexed events.
- Node.js and npm for the Hardhat contracts.
- An Ethereum-compatible JSON-RPC endpoint and deployed contract addresses to run the indexer.

## Smart contracts

From the `contracts/` directory:

```sh
npm install
npx hardhat compile
npx hardhat test
```

The contracts and deployment notes are in [`contracts/README.md`](./contracts/README.md). Contract deployment requires a configured network and wallet; do not commit private keys or RPC credentials.

## Event indexer

Create a PostgreSQL database, then configure the backend environment. At least one deployed contract address and a read-only JSON-RPC endpoint are required:

```text
INDEXER_RPC_URL=https://your-evm-rpc-endpoint
TREASURY_CONTRACT_ADDRESS=0x...
PAYROLL_CONTRACT_ADDRESS=0x...
SUBSCRIPTION_MANAGER_CONTRACT_ADDRESS=0x...
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chainerp
SPRING_DATASOURCE_USERNAME=chainerp
SPRING_DATASOURCE_PASSWORD=your-local-password
INDEXER_START_BLOCK=0
INDEXER_CONFIRMATIONS=3
INDEXER_CHUNK_SIZE=2000
```

Only configure addresses for contracts that have been deployed. From the `backend/` directory, start the service and run its tests with:

```sh
mvn spring-boot:run
mvn test
```

Flyway creates the event, cursor, and block-checkpoint tables on startup. Query indexed events using:

```text
GET /api/v1/events/contracts/{contract}/events?page=0&size=50
```

`contract` is the configured contract key, such as `treasury`; `page` is zero-based and `size` is limited to 100. See [`backend/README.md`](./backend/README.md) for configuration details.

## Documentation

- [Architecture](./docs/architecture.md)
- [Requirements](./docs/requirements.md)
- [API design](./docs/api.md)
- [Database design](./docs/database.md)
- [Roadmap](./docs/roadmap.md)

## Security and production readiness

The indexer uses a read-only RPC endpoint and does not need private signing keys. Protect the API with the planned authentication and authorization before exposing it beyond a trusted development environment. Production use also requires integration testing, deployment and monitoring work, and security and payroll-compliance reviews.
