package com.vitaledge.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base API exception carrying an HTTP status and a machine-readable error code.
 * Translates the source project's FastAPI {@code HTTPException} pattern.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}