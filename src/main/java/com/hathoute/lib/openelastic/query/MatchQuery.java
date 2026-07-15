package com.hathoute.lib.openelastic.query;

/** A full-text {@code match} query on the given {@code field} for {@code value}. */
public record MatchQuery(String field, Object value) implements Query {

    public MatchQuery {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
    }
}
