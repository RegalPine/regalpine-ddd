package io.github.regalpine.ddd.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainServiceTest {

    interface PricingService extends DomainService {
        int calculatePrice(int quantity);
    }

    @Test
    void domainServiceShouldBeImplementable() {
        PricingService service = quantity -> quantity * 100;
        assertThat(service).isInstanceOf(DomainService.class);
        assertThat(service.calculatePrice(3)).isEqualTo(300);
    }
}
