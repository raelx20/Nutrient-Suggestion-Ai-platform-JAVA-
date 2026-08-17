package com.vitaledge.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "AUTHORIZATION_ERROR", message);
    }
}