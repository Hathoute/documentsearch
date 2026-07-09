package com.hathoute.lib.openelastic;

/**
 * Unchecked exception thrown by {@link DocumentSearch} operations when the
 * underlying backend call fails (network, IO, transport error, ...).
 */
public class DocumentSearchException extends RuntimeException {

    public DocumentSearchException(String message) {
        super(message);
    }

    public DocumentSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
