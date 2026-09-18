package io.github.regalpine.ddd.infrastructure.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/** 借用连接执行 SQL；回调不得提交、回滚或关闭连接。 */
public interface JdbcConnectionAccess {
    <T> T execute(boolean requireTransaction, SqlWork<T> work);

    @FunctionalInterface
    interface SqlWork<T> {
        T execute(Connection connection) throws SQLException;
    }
}
