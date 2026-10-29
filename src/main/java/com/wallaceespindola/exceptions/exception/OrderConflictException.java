package com.wallaceespindola.exceptions.exception;

import org.springframework.http.HttpStatus;

public class OrderConflictException extends ApiException {

    public OrderConflictException(long id) {
        super(HttpStatus.CONFLICT, "ORDER_ALREADY_PAID", "Order %d is already paid".formatted(id));
    }
}
