# Architecture Overview

This document describes the chainERP architecture and the chosen stack for this repository.

## Chosen stack
- Frontend: React (Single Page Application)
- Backend: Spring Boot (Java) with Spring Security for RBAC
- Smart contracts: Solidity, developed and tested with Hardhat
- Database: PostgreSQL (primary transactional store)
- Event indexing: The Graph or a custom indexer that writes to an `events` table
- Async/queue: Kafka or RabbitMQ for integration and background jobs

## High-level components
- `frontend/` — React app communicating with backend via REST (or GraphQL)
- `backend/` — Spring Boot application exposing `/api/*` endpoints and handling business logic
- `contracts/` — Solidity contracts, tests, and deployment scripts
- `database` — Postgres schemas and migration files (Flyway/Liquibase recommended)
- `indexer` — event-indexing service (optional folder / service)

## Core modules
- Treasury: on-chain & off-chain flows for funds management
- Payroll: payroll runs and on-chain settlement where applicable
- Subscriptions: billing, renewal, and on-chain subscription records
- Event indexing: listen to contract events and reconcile into DB for fast queries

## Data flow (detailed)
1. User interacts with React frontend.
2. Frontend calls Spring Boot APIs (authenticated via JWT/OAuth2).
3. APIs persist domain state to Postgres and enqueue async jobs when needed.
4. For on-chain operations, backend submits transactions to Ethereum-compatible nodes via a signer service.
5. Contracts emit events; the indexer ingests events and writes normalized rows back to Postgres.

## Deployment
- Recommend containerized services (Docker) and orchestration (Kubernetes) for production.
- Secrets: manage private keys and node RPC credentials using a secure vault (HashiVault, AWS Secrets Manager).

## TODO
- Add deployment diagrams under `diagrams/` and record cluster sizing and backups.

