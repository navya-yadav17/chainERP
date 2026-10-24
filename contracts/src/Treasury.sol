// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

contract Treasury {
    address public immutable admin;

    event TransferRecorded(address indexed from, address indexed to, uint256 amount, uint256 timestamp);

    modifier onlyAdmin() {
        require(msg.sender == admin, "not admin");
        _;
    }

    constructor(address _admin) {
        require(_admin != address(0), "invalid admin");
        admin = _admin;
    }

    receive() external payable {}

    function transferTo(address payable to, uint256 amount) external onlyAdmin {
        require(to != address(0), "invalid recipient");
        require(amount > 0, "amount must be greater than zero");

        (bool success, ) = to.call{value: amount}("");
        require(success, "transfer failed");

        emit TransferRecorded(address(this), to, amount, block.timestamp);
    }
}
