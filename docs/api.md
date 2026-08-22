# API Reference (draft)

This document describes the public and internal APIs for chainERP. Concrete request/response examples and authentication details should be filled in after the backend implementation is finalized.

## Versioning
- API prefix: `/api/v1` (example)

## Authentication
- Recommended: JWT bearer tokens for service-to-service and user auth.
- Note: document OAuth2 / SSO flow if applicable.

## Core endpoints (examples)
- `POST /api/v1/auth/login` — authenticate user, return token
- `GET /api/v1/products` — list products
- `POST /api/v1/orders` — create order
- `GET /api/v1/orders/{id}` — retrieve order details
- `POST /api/v1/contracts/submit` — submit on-chain action (signed)

## Error handling
- Use consistent HTTP status codes and a JSON error envelope: `{ "error": { "code": "", "message": "" } }`.

## TODO
- Add concrete request/response schemas (OpenAPI/Swagger recommended).

