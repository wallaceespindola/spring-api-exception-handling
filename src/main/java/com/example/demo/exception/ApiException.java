package com.example.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Base class for domain errors: carries the HTTP status and a stable machine-readable code. */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
