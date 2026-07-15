package com.hathoute.lib.openelastic.query;

/**
 * Root of the lightweight query DSL. Maps to the native query types of both
 * the ElasticSearch and OpenSearch clients.
 */
public sealed interface Query permits MatchAllQuery, MatchQuery, TermQuery, BoolQuery,
        NumberRangeQuery, DateRangeQuery {

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

    // -- Numeric range -------------------------------------------------------

    static NumberRangeQuery gt(String field, double value) {
        return new NumberRangeQuery(field, value, null, null, null);
    }

    static NumberRangeQuery gte(String field, double value) {
        return new NumberRangeQuery(field, null, value, null, null);
    }

    static NumberRangeQuery lt(String field, double value) {
        return new NumberRangeQuery(field, null, null, value, null);
    }

    static NumberRangeQuery lte(String field, double value) {
        return new NumberRangeQuery(field, null, null, null, value);
    }

    static NumberRangeQuery.Builder num(String field) {
        return NumberRangeQuery.builder(field);
    }

    // -- Date range ----------------------------------------------------------

    static DateRangeQuery dateGt(String field, String value) {
        return new DateRangeQuery(field, value, null, null, null, null, null);
    }

    static DateRangeQuery dateGte(String field, String value) {
        return new DateRangeQuery(field, null, value, null, null, null, null);
    }

    static DateRangeQuery dateLt(String field, String value) {
        return new DateRangeQuery(field, null, null, value, null, null, null);
    }

    static DateRangeQuery dateLte(String field, String value) {
        return new DateRangeQuery(field, null, null, null, value, null, null);
    }

    static DateRangeQuery.Builder date(String field) {
        return DateRangeQuery.builder(field);
    }
}
