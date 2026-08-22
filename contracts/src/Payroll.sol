// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

contract Payroll {
    event PayrollRun(uint256 indexed runId, uint256 totalAmount, uint256 timestamp);

    function emitPayrollRun(uint256 runId, uint256 totalAmount) external {
        emit PayrollRun(runId, totalAmount, block.timestamp);
    }
}
