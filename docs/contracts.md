# Smart Contracts

Location: `contracts/` contains smart contract sources and test harnesses. This page documents the repository layout, testing and deployment suggestions.

## Suggested tools
- Development: Hardhat or Truffle
- Testing: Mocha/Chai (JS) or Foundry (Rust-based) depending on language
- Deployment: scripts under `contracts/src/test/scripts` — standardize to a single toolchain.

## Repository layout (expected)
- `contracts/src` — contract sources
- `contracts/src/test` — unit/integration tests
- `contracts/src/test/scripts` — deployment/test scripts

## Security
- Add a security checklist and plan a third-party audit before mainnet deployment.

## TODO
- Add concrete contract names, interfaces, and ABI export instructions.

