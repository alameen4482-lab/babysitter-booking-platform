package com.babysitterbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be found in the database.
 * Maps to HTTP 404 Not Found.
 *
 * <p>Example usage:
 * <pre>
 *   userRepository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException("User", id));
 * </pre>
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(
            HttpStatus.NOT_FOUND,
            String.format("%s not found with identifier: %s", resourceName, identifier)
        );
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
