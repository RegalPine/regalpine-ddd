package io.github.regalpine.ddd.application.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationServiceTest {

    // -- Test fixtures --

    static final class AllowAllAuthorizationService implements AuthorizationService {
        @Override
        public AuthorizationDecision authorize(AuthorizationRequest request) {
            return AuthorizationDecision.allow();
        }
    }

    static final class DenyAllAuthorizationService implements AuthorizationService {
        @Override
        public AuthorizationDecision authorize(AuthorizationRequest request) {
            return AuthorizationDecision.deny("Access denied for: " + request.operation());
        }
    }

    // -- Tests --

    @Test
    void shouldAuthorizeWhenAllowed() {
        AuthorizationService service = new AllowAllAuthorizationService();
        var decision = service.authorize(new AuthorizationRequest("PayOrder"));

        assertThat(decision.authorized()).isTrue();
        assertThat(decision.reason()).isNull();
    }

    @Test
    void shouldDenyWithReason() {
        AuthorizationService service = new DenyAllAuthorizationService();
        var decision = service.authorize(new AuthorizationRequest("CancelOrder"));

        assertThat(decision.authorized()).isFalse();
        assertThat(decision.reason()).contains("CancelOrder");
    }

    @Test
    void shouldCreateRequestWithFullContext() {
        var request = new AuthorizationRequest("PayOrder", "user-1", "tenant-1");

        assertThat(request.operation()).isEqualTo("PayOrder");
        assertThat(request.principalId()).isEqualTo("user-1");
        assertThat(request.tenantId()).isEqualTo("tenant-1");
    }

    @Test
    void shouldCreateRequestWithOperationOnly() {
        var request = new AuthorizationRequest("PayOrder");

        assertThat(request.operation()).isEqualTo("PayOrder");
        assertThat(request.principalId()).isNull();
        assertThat(request.tenantId()).isNull();
    }

    @Test
    void shouldRejectNullOperation() {
        assertThatThrownBy(() -> new AuthorizationRequest(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCreateAllowDecision() {
        var decision = AuthorizationDecision.allow();
        assertThat(decision.authorized()).isTrue();
    }

    @Test
    void shouldCreateDenyDecision() {
        var decision = AuthorizationDecision.deny("Not allowed");
        assertThat(decision.authorized()).isFalse();
        assertThat(decision.reason()).isEqualTo("Not allowed");
    }
}
