package com.hathoute.lib.openelastic;

/**
 * A single document to index as part of a {@link DocumentSearch#bulk} request.
 *
 * @param id       document identifier
 * @param document document payload
 * @param <T>      document type
 */
public record BulkEntry<T>(String id, T document) {

    public BulkEntry {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
    }

    public static <T> BulkEntry<T> of(String id, T document) {
        return new BulkEntry<>(id, document);
    }
}
