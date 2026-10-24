package com.chainerp.backend.indexer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.TypeReference;

public final class KnownContractEvents {
    private static final Event TRANSFER_RECORDED = new Event(
        "TransferRecorded",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Address>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {},
            new TypeReference<Uint256>() {}
        )
    );
    private static final Event PAYROLL_RUN = new Event(
        "PayrollRun",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Uint256>(true) {},
            new TypeReference<Uint256>() {},
            new TypeReference<Uint256>() {}
        )
    );
    private static final Event SUBSCRIPTION_RENEWED = new Event(
        "SubscriptionRenewed",
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Bytes32>(true) {},
            new TypeReference<Address>(true) {},
            new TypeReference<Uint256>() {}
        )
    );
    private static final Map<String, String> NAMES_BY_TOPIC = Arrays.asList(
        TRANSFER_RECORDED, PAYROLL_RUN, SUBSCRIPTION_RENEWED
    ).stream().collect(Collectors.toUnmodifiableMap(
        event -> EventEncoder.encode(event).toLowerCase(),
        Event::getName
    ));

    private KnownContractEvents() {
    }

    public static String nameForTopic(String topic) {
        return NAMES_BY_TOPIC.getOrDefault(topic.toLowerCase(), "Unknown");
    }

    public static String topicForName(String name) {
        return NAMES_BY_TOPIC.entrySet().stream()
            .filter(entry -> entry.getValue().equals(name))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown contract event: " + name));
    }
}
