require("@nomicfoundation/hardhat-toolbox");

module.exports = {
  solidity: "0.8.17",
  paths: {
    sources: "./src",
    tests: "./test",
    cache: "./cache",
    artifacts: "./artifacts"
  },
  networks: {
    hardhat: {},
    localhost: {
      url: "http://127.0.0.1:8545"
    }
  }
};
