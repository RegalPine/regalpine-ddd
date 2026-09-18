package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.parameter.ParameterBindings;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;

/**
 * Context for the SQL translation process.
 *
 * <p>Phase XIII §29: holds the parameter bindings collector and optional
 * pagination dialect during translation.</p>
 *
 * @author RegalPine
 */
public final class TranslationContext {

    private final ParameterBindings bindings;
    private final PaginationDialect dialect;

    public TranslationContext(PaginationDialect dialect) {
        this.bindings = new ParameterBindings();
        this.dialect = dialect;
    }

    public TranslationContext() {
        this(null);
    }

    public ParameterBindings bindings() {
        return bindings;
    }

    public PaginationDialect dialect() {
        return dialect;
    }
}
