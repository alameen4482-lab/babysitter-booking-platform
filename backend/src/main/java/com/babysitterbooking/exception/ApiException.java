package com.babysitterbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Base runtime exception for all application-specific errors.
 *
 * <p>Carries an {@link HttpStatus} so the {@link GlobalExceptionHandler}
 * can map it to the correct HTTP response code without needing separate
 * exception types for each status code.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
