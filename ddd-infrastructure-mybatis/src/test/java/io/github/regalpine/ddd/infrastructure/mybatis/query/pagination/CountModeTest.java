package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

import io.github.regalpine.ddd.core.pagination.CountMode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CountMode - strategy selection (§47, §73)")
class CountModeTest {

    @Test
    @DisplayName("default CountMode is EXACT")
    void defaultCountMode() {
        var wrapper = Wrappers.<Object>query();
        assertEquals(CountMode.EXACT, wrapper.countMode());
    }

    @Test
    @DisplayName("CountMode can be changed to NONE")
    void noneCountMode() {
        var wrapper = Wrappers.<Object>query().countMode(CountMode.NONE);
        assertEquals(CountMode.NONE, wrapper.countMode());
    }

    @Test
    @DisplayName("CountMode can be changed to ESTIMATED")
    void estimatedCountMode() {
        var wrapper = Wrappers.<Object>query().countMode(CountMode.ESTIMATED);
        assertEquals(CountMode.ESTIMATED, wrapper.countMode());
    }

    @Test
    @DisplayName("CountMode can be changed to AUTO")
    void autoCountMode() {
        var wrapper = Wrappers.<Object>query().countMode(CountMode.AUTO);
        assertEquals(CountMode.AUTO, wrapper.countMode());
    }

    @Test
    @DisplayName("CountMode is preserved in QueryPlan")
    void countModeInPlan() {
        var plan = Wrappers.<Object>query()
                .countMode(CountMode.NONE)
                .freeze();
        assertEquals(CountMode.NONE, plan.countMode());
    }

    @Test
    @DisplayName("all CountMode values exist")
    void allValues() {
        assertEquals(4, CountMode.values().length);
        assertNotNull(CountMode.EXACT);
        assertNotNull(CountMode.NONE);
        assertNotNull(CountMode.ESTIMATED);
        assertNotNull(CountMode.AUTO);
    }
}
