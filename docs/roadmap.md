# chainERP Roadmap

This document outlines milestones now aligned to the chosen stack: Solidity contracts, Spring Boot backend, and React frontend.

## Goals
- Deliver an MVP with on-chain auditable flows for treasury, payroll, and subscriptions.

## Milestones (suggested order)
1. Project setup & tooling (Hardhat, Spring Boot scaffold, React scaffold)
2. Smart contract design: Treasury, Payroll hooks, SubscriptionManager
3. Backend: implement Spring Boot services for Treasury, Payroll, Subscriptions, RBAC, and event ingestion
4. Indexer: implement event indexer (The Graph or custom) and `contract_events` table
5. Frontend: implement React UI for finance/HR dashboards and subscription management
6. Integration testing and E2E (local & testnet)
7. Security audits (contracts + backend), compliance checks for payroll
8. Production deployment and monitoring

## Notes / Next actions
- Assign owners for each milestone and add sprint dates.
- Prepare audit budget and testnet deployment plan.

