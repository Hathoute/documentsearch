package com.hathoute.lib.openelastic.query;

/**
 * A numeric {@code range} query on the given {@code field} with optional lower
 * and upper bounds.
 *
 * <p>Bounds are exclusive ({@code gt}/{@code lt}) or inclusive
 * ({@code gte}/{@code lte}).</p>
 *
 * <p>Example: {@code Query.num("price").gt(10).lte(100).build()}</p>
 */
public record NumberRangeQuery(String field, Double gt, Double gte, Double lt, Double lte) implements Query {

    public NumberRangeQuery {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
        if (gt == null && gte == null && lt == null && lte == null) {
            throw new IllegalArgumentException("at least one range bound must be set");
        }
    }

    public static Builder builder(String field) {
        return new Builder(field);
    }

    public static final class Builder {
        private final String field;
        private Double gt;
        private Double gte;
        private Double lt;
        private Double lte;

        private Builder(String field) {
            this.field = field;
        }

        public Builder gt(double value) {
            this.gt = value;
            return this;
        }

        public Builder gte(double value) {
            this.gte = value;
            return this;
        }

        public Builder lt(double value) {
            this.lt = value;
            return this;
        }

        public Builder lte(double value) {
            this.lte = value;
            return this;
        }

        public NumberRangeQuery build() {
            return new NumberRangeQuery(field, gt, gte, lt, lte);
        }
    }
}
