# Smart Contracts (Solidity)

Contracts are written in Solidity and live in `contracts/`. Use Hardhat as the development toolchain (recommended) with plugins for testing and ABI generation.

## Recommended contracts (by module)
- `Treasury.sol` — handles fund custody, transfers, and role-based treasury operations
- `Payroll.sol` — minimal on-chain hooks for payroll settlement or attestations
- `SubscriptionManager.sol` — stores subscription entitlements and emits events on renew/expire
- `ERPEvents.sol` — shared event definitions for off-chain indexing

## Tooling
- Hardhat for compilation, testing, and deployments
- Ethers.js for scripts and tests
- Mocha/Chai for unit tests
- Export ABIs to `contracts/abi/` for backend consumption

## Events (example)
- `event TransferRecorded(address indexed from, address indexed to, uint256 amount, uint256 timestamp)`
- `event PayrollRun(uint256 indexed runId, uint256 totalAmount, uint256 timestamp)`
- `event SubscriptionRenewed(bytes32 indexed subscriptionId, address subscriber, uint256 timestamp)`

## Testing & Deployment
- Keep deployment scripts deterministic and environment-aware (rinkeby/localhost/mainnet). Use environment variables for private keys and RPC endpoints.

## Security
- Run a security checklist: unit tests, coverage, fuzzing, and plan a third-party audit before production.

## TODO
- Add exact contract interfaces, solidity versions, and ABI export steps.

