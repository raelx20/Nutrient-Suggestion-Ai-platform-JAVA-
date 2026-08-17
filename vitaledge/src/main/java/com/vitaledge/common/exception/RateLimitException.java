package com.vitaledge.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitException extends ApiException {

    private final int retryAfterSeconds;

    public RateLimitException(String message) {
        this(message, 60);
    }

    public RateLimitException(String message, int retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}