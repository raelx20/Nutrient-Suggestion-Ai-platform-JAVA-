package com.vitaledge.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", message);
    }
}