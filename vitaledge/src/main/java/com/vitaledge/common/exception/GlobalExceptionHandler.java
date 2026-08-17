package com.vitaledge.common.exception;

import com.vitaledge.web.dto.error.ErrorResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handling producing the platform's standardized error envelope.
 * Mirrors the source project's FastAPI exception handlers: no internal details leak.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        ErrorResponse body = ErrorResponse.of(ex.getCode(), ex.getMessage());
        if (ex instanceof RateLimitException rl) {
            return ResponseEntity.status(ex.getStatus())
                    .header("Retry-After", String.valueOf(rl.getRetryAfterSeconds()))
                    .body(body);
        }
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Validation failed", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Invalid request body", List.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Invalid request parameter", List.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("AUTHORIZATION_ERROR", "Insufficient permissions"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred. Please try again later."));
    }

    private ErrorResponse.ErrorDetail toDetail(FieldError error) {
        return new ErrorResponse.ErrorDetail(error.getField(), error.getDefaultMessage(), "INVALID_FORMAT");
    }
}