// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

contract SubscriptionManager {
    event SubscriptionRenewed(bytes32 indexed subscriptionId, address indexed subscriber, uint256 timestamp);

    function renewSubscription(bytes32 subscriptionId, address subscriber) external {
        emit SubscriptionRenewed(subscriptionId, subscriber, block.timestamp);
    }
}
