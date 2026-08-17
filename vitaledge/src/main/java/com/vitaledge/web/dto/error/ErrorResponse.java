package com.vitaledge.web.dto.error;

import java.util.List;

/**
 * Standardized error response: {@code { "error": { "code": ..., "message": ..., "details": [...] } }}.
 */
public record ErrorResponse(
        ErrorBody error
) {
    public ErrorResponse {
        if (error == null) {
            error = new ErrorBody("INTERNAL_SERVER_ERROR", "An unexpected error occurred", List.of());
        }
    }

    public static ErrorResponse of(String code, String message, List<ErrorDetail> details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message, List.of()));
    }

    public record ErrorBody(
            String code,
            String message,
            List<ErrorDetail> details
    ) {
    }

    public record ErrorDetail(
            String field,
            String message,
            String code
    ) {
    }
}