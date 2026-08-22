// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

contract Treasury {
    address public admin;

    event TransferRecorded(address indexed from, address indexed to, uint256 amount, uint256 timestamp);

    modifier onlyAdmin() {
        require(msg.sender == admin, "not admin");
        _;
    }

    constructor(address _admin) {
        admin = _admin;
    }

    function recordTransfer(address to, uint256 amount) external onlyAdmin {
        emit TransferRecorded(msg.sender, to, amount, block.timestamp);
    }
}
