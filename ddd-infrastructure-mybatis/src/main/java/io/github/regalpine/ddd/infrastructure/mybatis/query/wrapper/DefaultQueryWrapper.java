package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

/**
 * Default implementation of {@link QueryWrapper}.
 *
 * <p>Phase XIII §20/§21: created via {@code Wrappers.query()}.</p>
 *
 * @param <T> the result type marker
 * @author RegalPine
 */
public class DefaultQueryWrapper<T> extends AbstractQueryWrapper<T> {

    @Override
    protected NestedQueryWrapper<T> createNested() {
        return new NestedQueryWrapper<>();
    }
}
