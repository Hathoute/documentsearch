package com.hathoute.lib.openelastic;

/** A {@code term} (not analyzed) query on the given {@code field} for {@code value}. */
public record TermQuery(String field, Object value) implements Query {

    public TermQuery {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
    }
}
