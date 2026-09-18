package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueryWrapper - conditional chain (§22)")
class ConditionalQueryTest {

    static final QueryField<String> STATUS = QueryFields.of("status", String.class);
    static final QueryField<String> NAME = QueryFields.of("name", String.class);

    @Test
    @DisplayName("eq(true, ...) adds condition")
    void conditionalTrue() {
        var wrapper = Wrappers.<Object>query()
                .eq(true, STATUS, "PAID");
        assertEquals(1, wrapper.nodes().size());
    }

    @Test
    @DisplayName("eq(false, ...) skips condition")
    void conditionalFalse() {
        var wrapper = Wrappers.<Object>query()
                .eq(false, STATUS, "PAID");
        assertEquals(0, wrapper.nodes().size());
    }

    @Test
    @DisplayName("mixed conditional and unconditional")
    void mixedConditional() {
        String status = "PAID";
        String name = null;

        var wrapper = Wrappers.<Object>query()
                .eq(status != null, STATUS, status)
                .eq(name != null, NAME, name);

        assertEquals(1, wrapper.nodes().size());
    }
}
