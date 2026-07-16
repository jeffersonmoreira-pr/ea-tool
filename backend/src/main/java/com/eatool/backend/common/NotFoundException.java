package com.eatool.backend.common;

/**
 * The requested record does not exist. Mapped to HTTP 404 by
 * {@link ApiExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
