package com.babysitterbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a concurrent booking conflict is detected.
 * Maps to HTTP 409 Conflict.
 *
 * <p>This exception is the primary response when JPA optimistic locking
 * detects that two simultaneous requests attempted to book the same
 * {@code AvailabilitySlot} and only one can succeed.
 *
 * <p>The {@link GlobalExceptionHandler} also maps
 * {@code jakarta.persistence.OptimisticLockException} to this same
 * HTTP status for transparent handling.
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ConflictException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, message, cause);
    }
}
