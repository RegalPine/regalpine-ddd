package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.infrastructure.exception.PersistenceAccessException;
import io.github.regalpine.ddd.infrastructure.exception.PersistenceConnectionException;
import io.github.regalpine.ddd.transaction.TransactionDefinition;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JdbcTransactionAdapter} lifecycle and exception mapping.
 */
@DisplayName("JdbcTransactionAdapter")
class JdbcTransactionAdapterTest {

    private ConnectionProvider connectionProvider;
    private JdbcTransactionAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        connectionProvider = () -> {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testTx;DB_CLOSE_DELAY=-1", "sa", "");
            return conn;
        };
        adapter = new JdbcTransactionAdapter(connectionProvider);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (adapter.isActive()) {
            adapter.rollback();
            adapter.close();
        }
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }

    @Test
    @DisplayName("initially not active")
    void initiallyInactive() {
        assertFalse(adapter.isActive());
    }

    @Test
    @DisplayName("begin activates, commit deactivates")
    void beginAndCommit() {
        adapter.begin(TransactionDefinition.DEFAULT);
        assertTrue(adapter.isActive());

        adapter.commit();
        adapter.close();
        assertFalse(adapter.isActive());
    }

    @Test
    @DisplayName("begin activates, rollback deactivates")
    void beginAndRollback() {
        adapter.begin(TransactionDefinition.DEFAULT);
        assertTrue(adapter.isActive());

        adapter.rollback();
        adapter.close();
        assertFalse(adapter.isActive());
    }

    @Test
    @DisplayName("connection failure throws PersistenceConnectionException")
    void connectionFailureMapsToPersistenceException() {
        ConnectionProvider failingProvider = () -> {
            throw new SQLException("simulated connection failure");
        };
        JdbcTransactionAdapter failingAdapter = new JdbcTransactionAdapter(failingProvider);

        assertThrows(PersistenceConnectionException.class,
                () -> failingAdapter.begin(TransactionDefinition.DEFAULT));
    }
}
