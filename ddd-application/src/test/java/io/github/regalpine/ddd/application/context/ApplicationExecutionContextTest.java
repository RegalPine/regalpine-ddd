package io.github.regalpine.ddd.application.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExecutionContextTest {

    // -- Test fixtures --

    record TestExecutionContext(
            String commandId,
            String correlationId,
            String causationId,
            String tenantId,
            String principalId
    ) implements ApplicationExecutionContext {}

    // -- Tests --

    @Test
    void shouldReturnCommandId() {
        ApplicationExecutionContext ctx = new TestExecutionContext(
                "cmd-1", "corr-1", "caus-1", "tenant-1", "user-1");
        assertThat(ctx.commandId()).isEqualTo("cmd-1");
    }

    @Test
    void shouldReturnCorrelationId() {
        ApplicationExecutionContext ctx = new TestExecutionContext(
                "cmd-1", "corr-1", "caus-1", "tenant-1", "user-1");
        assertThat(ctx.correlationId()).isEqualTo("corr-1");
    }

    @Test
    void shouldReturnCausationId() {
        ApplicationExecutionContext ctx = new TestExecutionContext(
                "cmd-1", "corr-1", "caus-1", "tenant-1", "user-1");
        assertThat(ctx.causationId()).isEqualTo("caus-1");
    }

    @Test
    void shouldReturnTenantId() {
        ApplicationExecutionContext ctx = new TestExecutionContext(
                "cmd-1", "corr-1", "caus-1", "tenant-1", "user-1");
        assertThat(ctx.tenantId()).isEqualTo("tenant-1");
    }

    @Test
    void shouldReturnPrincipalId() {
        ApplicationExecutionContext ctx = new TestExecutionContext(
                "cmd-1", "corr-1", "caus-1", "tenant-1", "user-1");
        assertThat(ctx.principalId()).isEqualTo("user-1");
    }

    @Test
    void shouldAllowNullValues() {
        ApplicationExecutionContext ctx = new TestExecutionContext(
                null, null, null, null, null);
        assertThat(ctx.commandId()).isNull();
        assertThat(ctx.correlationId()).isNull();
        assertThat(ctx.tenantId()).isNull();
    }
}
