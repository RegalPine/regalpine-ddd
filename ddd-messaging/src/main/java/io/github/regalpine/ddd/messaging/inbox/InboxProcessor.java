package io.github.regalpine.ddd.messaging.inbox;

import java.util.Objects;
import java.util.function.Consumer;

/** 原子 Inbox 消费；事务边界必须在返回前真正完成提交。 */
public final class InboxProcessor {
    private final InboxStore store;
    private final Consumer<Runnable> transaction;

    public InboxProcessor(InboxStore store, Consumer<Runnable> transaction) {
        this.store = Objects.requireNonNull(store, "store");
        this.transaction = Objects.requireNonNull(transaction, "transaction");
    }

    public void process(String consumerId, String messageId, String eventType, Consumer<InboxRecord> handler) {
        Objects.requireNonNull(handler, "handler");
        InboxRecord record = InboxRecord.create(messageId, consumerId, eventType);
        transaction.accept(() -> {
            if (!store.tryInsert(record)) {
                if (!store.isProcessed(consumerId, messageId)) {
                    throw new IllegalStateException("重复 Inbox 记录尚未完成，不能确认消息");
                }
                return;
            }
            handler.accept(record);
            store.markProcessed(consumerId, messageId);
        });
    }
}
