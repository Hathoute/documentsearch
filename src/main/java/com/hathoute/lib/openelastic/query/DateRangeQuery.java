package com.hathoute.lib.openelastic.query;

/**
 * A date {@code range} query on the given {@code field} with optional lower
 * and upper bounds, date {@code format}, and {@code timeZone}.
 *
 * <p>Bounds are exclusive ({@code gt}/{@code lt}) or inclusive
 * ({@code gte}/{@code lte}). Bound values are date strings parsed according
 * to the specified {@code format} (e.g. {@code yyyy-MM-dd}) or the field's
 * mapping format.</p>
 *
 * <p>Example: {@code Query.date("created").gte("2024-01-01").lt("2024-12-31")
 *     .format("yyyy-MM-dd").timeZone("UTC").build()}</p>
 */
public record DateRangeQuery(String field, String gt, String gte, String lt, String lte,
                             String format, String timeZone) implements Query {

    public DateRangeQuery {
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
        private String gt;
        private String gte;
        private String lt;
        private String lte;
        private String format;
        private String timeZone;

        private Builder(String field) {
            this.field = field;
        }

        public Builder gt(String value) {
            this.gt = value;
            return this;
        }

        public Builder gte(String value) {
            this.gte = value;
            return this;
        }

        public Builder lt(String value) {
            this.lt = value;
            return this;
        }

        public Builder lte(String value) {
            this.lte = value;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder timeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public DateRangeQuery build() {
            return new DateRangeQuery(field, gt, gte, lt, lte, format, timeZone);
        }
    }
}
