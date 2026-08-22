# Requirements

This file captures functional and non-functional requirements for the chosen stack: Solidity contracts, Spring Boot backend, React frontend.

## Functional requirements (modules)
- Treasury: create/approve transfers, on-chain settlement, reporting
- Payroll: scheduled payroll runs, tax calculations, archiving results
- Subscriptions: recurring billing, plan changes, cancellations
- Event indexing: ingest contract events and expose queryable records
- RBAC: fine-grained roles for finance, HR, admin, and service accounts

## Non-functional requirements
- Security: protect signing keys; use HSM or vault for private keys; Spring Security for RBAC
- Compliance: payroll must generate auditable artifacts and support export for regulatory reporting
- Reliability: ensure idempotent on-chain submission and reconcilation strategies for failed txs
- Performance: event indexer should process blocks with low latency (configurable SLA)

## Acceptance criteria (examples)
- Treasury transfers can be approved by `ROLE_FINANCE` and reflect on-chain within SLA
- Payroll run generates `payroll_runs` and triggers correct treasury transfers
- Subscriptions bill at correct cadence and emit `SubscriptionRenewed` events

## TODO
- Define exact SLAs, backup/DR, and compliance checklist for payroll in target jurisdictions.

