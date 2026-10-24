package com.chainerp.backend.indexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KnownContractEventsTest {
    @Test
    void mapsDocumentedEventTopicsToNames() {
        assertThat(KnownContractEvents.nameForTopic(
            KnownContractEvents.topicForName("TransferRecorded")
        )).isEqualTo("TransferRecorded");
        assertThat(KnownContractEvents.nameForTopic(
            KnownContractEvents.topicForName("PayrollRun")
        )).isEqualTo("PayrollRun");
        assertThat(KnownContractEvents.nameForTopic(
            KnownContractEvents.topicForName("SubscriptionRenewed")
        )).isEqualTo("SubscriptionRenewed");
    }

    @Test
    void preservesUnknownTopicsWithoutDiscardingTheirLogs() {
        assertThat(KnownContractEvents.nameForTopic("0x1234")).isEqualTo("Unknown");
    }

    @Test
    void rejectsUndocumentedEventNames() {
        assertThatThrownBy(() -> KnownContractEvents.topicForName("NotAnEvent"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
