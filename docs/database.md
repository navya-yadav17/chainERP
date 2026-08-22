# Database

This document describes the primary data model and persistence recommendations.

## Recommended schema (core entities)
- `users` — authentication and profile
- `organizations` — tenant or company-level data
- `products` — SKU and inventory metadata
- `orders` — order lifecycle and status
- `shipments` — tracking and logistics
- `audit_events` — immutable event log for reconciliations

## Recommendations
- Use Postgres for relational integrity and powerful querying.
- Use migrations (Flyway, Liquibase, or ORM migrations) to manage schema changes.

## TODO
- Add ER diagram and concrete column definitions.

