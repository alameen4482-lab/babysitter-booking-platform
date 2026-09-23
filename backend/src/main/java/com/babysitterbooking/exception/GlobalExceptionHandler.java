package com.babysitterbooking.exception;

import com.babysitterbooking.dto.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 *
 * <p>Translates exceptions into the standard {@link ApiResponse} envelope
 * with appropriate HTTP status codes. This keeps controller code clean —
 * controllers simply throw exceptions; this handler formats the response.
 *
 * <p>Key mappings:
 * <ul>
 *   <li>{@link ApiException} (and subclasses) — status from exception</li>
 *   <li>{@link OptimisticLockException} — 409 Conflict (double-booking)</li>
 *   <li>{@link MethodArgumentNotValidException} — 400 with field error map</li>
 *   <li>{@link BadCredentialsException} — 401 Unauthorized</li>
 *   <li>{@link AccessDeniedException} — 403 Forbidden</li>
 *   <li>{@link Exception} (catch-all) — 500 Internal Server Error</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Application-Specific Exceptions
    // -------------------------------------------------------------------------

    /**
     * Handles all {@link ApiException} subclasses (ResourceNotFoundException,
     * ConflictException, etc.) using the status embedded in the exception.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.warn("ApiException [{}]: {}", ex.getStatus(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // JPA Concurrency — Optimistic Lock
    // -------------------------------------------------------------------------

    /**
     * Handles JPA optimistic lock failures.
     * This occurs when two threads simultaneously modify the same AvailabilitySlot.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(OptimisticLockException ex) {
        log.warn("Optimistic lock conflict detected: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                    "The requested slot is no longer available due to a concurrent booking. Please try again."
                ));
    }

    // -------------------------------------------------------------------------
    // Validation Exceptions
    // -------------------------------------------------------------------------

    /**
     * Handles @Valid/@Validated failures on request body fields.
     * Returns HTTP 400 with a map of field -> error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.debug("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed"));
    }

    /**
     * Handles constraint violations at the service/repository level.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {
        log.debug("Constraint violation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Constraint violation: " + ex.getMessage()));
    }

    /**
     * Handles path variable / query parameter type mismatches.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format(
            "Parameter '%s' has invalid value '%s'", ex.getName(), ex.getValue()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    // -------------------------------------------------------------------------
    // Security Exceptions
    // -------------------------------------------------------------------------

    /**
     * Handles failed authentication attempts (wrong password, invalid JWT).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password."));
    }

    /**
     * Handles authentication attempts by deactivated users.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
        log.warn("Disabled account login attempt: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Your account has been deactivated. Please contact an administrator."));
    }

    /**
     * Handles authorization failures (user lacks required role).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You do not have permission to perform this action."));
    }

    // -------------------------------------------------------------------------
    // Catch-All
    // -------------------------------------------------------------------------

    /**
     * Catch-all handler for any unhandled exception.
     * Logs the full stack trace and returns a generic 500 response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please contact support."));
    }
}
