package com.hathoute.lib.openelastic;

/**
 * Root of the lightweight query DSL. Maps to the native query types of both
 * the ElasticSearch and OpenSearch clients.
 *
 * <p>For queries not covered by this DSL (range, aggregations, ...), use the
 * JSON pass-through overload of {@link DocumentSearch#search(String, String,
 * Class)}.</p>
 */
public sealed interface Query permits MatchAllQuery, MatchQuery, TermQuery, BoolQuery {

    static MatchAllQuery matchAll() {
        return new MatchAllQuery();
    }

    static MatchQuery match(String field, Object value) {
        return new MatchQuery(field, value);
    }

    static TermQuery term(String field, Object value) {
        return new TermQuery(field, value);
    }

    static BoolQuery.Builder bool() {
        return BoolQuery.builder();
    }
}
