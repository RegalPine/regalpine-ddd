package io.github.regalpine.ddd.event.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** 所有修改操作均要求调用方建立短事务。 */
public interface LeasedOutboxStore extends OutboxStore {
    List<OutboxRecord> claim(int limit, String token, Instant now, Duration lease);
    boolean acknowledge(String outboxId, String token, Instant now);
    boolean fail(String outboxId, String token, Instant nextAttemptAt, String error, int maxAttempts);
    boolean requeue(String outboxId, Instant now);
}
