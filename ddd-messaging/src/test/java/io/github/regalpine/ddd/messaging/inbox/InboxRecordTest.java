package io.github.regalpine.ddd.messaging.inbox;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InboxRecordTest {

    @Test
    void createShouldSetReceivedStatus() {
        InboxRecord record = InboxRecord.create("e1", "c1", "TestEvent");

        assertThat(record.status()).isEqualTo("RECEIVED");
        assertThat(record.attempts()).isZero();
        assertThat(record.processedAt()).isNull();
    }

    @Test
    void markProcessedShouldUpdateStatus() {
        InboxRecord record = InboxRecord.create("e1", "c1", "TestEvent");
        InboxRecord processed = record.markProcessed();

        assertThat(processed.status()).isEqualTo("PROCESSED");
        assertThat(processed.processedAt()).isNotNull();
    }

    @Test
    void recordFailureShouldIncrementAttempts() {
        InboxRecord record = InboxRecord.create("e1", "c1", "TestEvent");
        InboxRecord failed = record.recordFailure("timeout");

        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.attempts()).isEqualTo(1);
        assertThat(failed.error()).isEqualTo("timeout");
    }
}
