# API Reference

Backend: Spring Boot REST APIs under the `/api/v1` prefix. Use OpenAPI/Swagger generation from SpringFox or `springdoc-openapi`.

## Authentication & Authorization
- Use JWT bearer tokens and `Spring Security` for RBAC.
- Roles: `ROLE_ADMIN`, `ROLE_FINANCE`, `ROLE_HR`, `ROLE_USER`, `ROLE_SERVICE`.

## Common patterns
- Use consistent JSON envelopes and proper HTTP status codes.
- Expose OpenAPI at `/api/v1/openapi.json` and Swagger UI for development.

## Core endpoints (selected)
- Auth
	- `POST /api/v1/auth/login` — returns JWT
	- `POST /api/v1/auth/refresh` — refresh token

- Treasury
	- `GET /api/v1/treasury/balance` — view balances (RBAC: FINANCE)
	- `POST /api/v1/treasury/transfer` — create transfer (RBAC: FINANCE)
	- `GET /api/v1/treasury/transactions` — list transactions

- Payroll
	- `POST /api/v1/payroll/run` — execute payroll run (RBAC: HR/FINANCE)
	- `GET /api/v1/payroll/{id}` — payroll run status and results

- Subscriptions
	- `POST /api/v1/subscriptions` — create subscription
	- `POST /api/v1/subscriptions/{id}/renew` — renew subscription
	- `GET /api/v1/subscriptions/{id}` — subscription details

- Contracts / On-chain
	- `POST /api/v1/contracts/tx` — submit signed transaction (service role)
	- `GET /api/v1/events` — query indexed contract events

## Event indexing API
- `GET /api/v1/events/contracts/{contract}/events` — paginated contract events

## Error handling
- Use a consistent JSON error envelope: `{ "error": { "code": "", "message": "", "details": {} } }`.

## TODO
- Add request/response DTOs and example `curl` snippets; generate OpenAPI spec from code.

