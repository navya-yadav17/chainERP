# Database

Primary datastore: PostgreSQL. Use Flyway or Liquibase for schema migrations and versioning.

## Core entities (concrete sketches)
- `users` (id, email, name, role, organization_id, created_at)
- `organizations` (id, name, metadata, created_at)
- `products` (id, sku, name, description, inventory_count)
- `orders` (id, organization_id, status, total_amount, created_at)

## Financial modules tables
- `treasury_transactions`
	- `id`, `organization_id`, `tx_hash` (nullable until on-chain), `from_account`, `to_account`, `amount`, `currency`, `status`, `metadata`, `created_at`

- `payroll_runs`
	- `id`, `organization_id`, `run_date`, `total_amount`, `status`, `generated_by`, `created_at`

- `payroll_entries`
	- `id`, `payroll_run_id`, `employee_id`, `gross_amount`, `net_amount`, `taxes`, `status`

- `subscriptions`
	- `id`, `organization_id`, `subscriber`, `plan`, `status`, `next_billing_date`, `metadata`

## Event indexing
- `contract_events`
	- `id`, `chain_id`, `contract_name`, `contract_address`, `event_name`, `transaction_hash`, `log_index`, `block_number`, `block_hash`, `indexed_at`, `payload_json`
- `indexer_cursors`
	- `chain_id`, `contract_name`, `contract_address`, `last_processed_block`, `last_processed_block_hash`, `updated_at`
- `indexer_checkpoints`
	- `chain_id`, `contract_address`, `block_number`, `block_hash`

## Recommendations
- Store minimal on-chain references (tx hash, contract address) and reconcile with the DB using the indexer.
- Encrypt sensitive fields and use column-level policies for access control where needed.

## TODO
- Create ER diagrams for the application data model.
