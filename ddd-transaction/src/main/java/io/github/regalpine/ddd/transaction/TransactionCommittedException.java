package io.github.regalpine.ddd.transaction;

/** 数据库已经提交，但后续通知或资源清理失败；调用方不得重试业务写入。 */
public final class TransactionCommittedException extends RuntimeException {
    public TransactionCommittedException(Throwable cause) {
        super("事务已提交，后续处理失败；不得自动重试业务", cause);
    }
}
