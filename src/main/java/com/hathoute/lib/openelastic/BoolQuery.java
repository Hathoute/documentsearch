package com.hathoute.lib.openelastic;

import java.util.ArrayList;
import java.util.List;

/**
 * A compound {@code bool} query combining clauses with {@code must} (AND),
 * {@code should} (OR) and {@code filter} (AND, not scored).
 *
 * <p>Example: {@code BoolQuery.builder()
 *     .must(Query.match("name", "bike"))
 *     .filter(Query.term("year", 2024))
 *     .build();}</p>
 */
public final class BoolQuery implements Query {

    private final List<Query> must;
    private final List<Query> should;
    private final List<Query> filter;

    private BoolQuery(List<Query> must, List<Query> should, List<Query> filter) {
        this.must = List.copyOf(must);
        this.should = List.copyOf(should);
        this.filter = List.copyOf(filter);
    }

    public List<Query> must() {
        return must;
    }

    public List<Query> should() {
        return should;
    }

    public List<Query> filter() {
        return filter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<Query> must = new ArrayList<>();
        private final List<Query> should = new ArrayList<>();
        private final List<Query> filter = new ArrayList<>();

        public Builder must(Query query) {
            must.add(query);
            return this;
        }

        public Builder should(Query query) {
            should.add(query);
            return this;
        }

        public Builder filter(Query query) {
            filter.add(query);
            return this;
        }

        public BoolQuery build() {
            return new BoolQuery(must, should, filter);
        }
    }
}
