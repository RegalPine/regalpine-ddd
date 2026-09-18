package io.github.regalpine.ddd.infrastructure.mybatis.mapping;

/**
 * Bidirectional mapper between a Domain Aggregate and a MyBatis persistence record.
 *
 * <p>Phase XII §10: the mapper converts aggregates to database records
 * for persistence and reconstructs aggregates from records on retrieval.
 * The domain model never depends on MyBatis types.</p>
 *
 * @param <A> the aggregate type
 * @param <R> the persistence record type
 * @author RegalPine
 */
public interface AggregateMapper<A, R> {

    /**
     * Converts an aggregate to its persistence record representation.
     *
     * @param aggregate the aggregate to map
     * @return the persistence record
     */
    R toRecord(A aggregate);

    /**
     * Converts a persistence record back to a domain aggregate.
     *
     * @param record the persistence record
     * @return the reconstructed aggregate
     */
    A toAggregate(R record);
}
