package com.eatool.backend.common;

/**
 * A request could not be processed because of invalid input (missing
 * required field, invalid enum/date value, or a foreign key referencing a
 * record that does not exist). Mapped to HTTP 400 by {@link ApiExceptionHandler}.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
