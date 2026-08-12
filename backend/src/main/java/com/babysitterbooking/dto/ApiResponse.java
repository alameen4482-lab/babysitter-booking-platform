package com.babysitterbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * Standard API response envelope used across all endpoints.
 *
 * <p>Every REST response from this platform follows this JSON shape:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Operation completed successfully.",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <p>The {@code data} field is omitted from the JSON output when it is {@code null},
 * keeping error responses clean.
 *
 * @param <T> The type of the payload carried in the {@code data} field.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    // Private constructor — use static factory methods below
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // -------------------------------------------------------------------------
    // Static Factory Methods
    // -------------------------------------------------------------------------

    /**
     * Creates a successful response carrying a data payload.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates a successful response with no payload (e.g., delete operations).
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Creates a failure response (no data payload).
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
