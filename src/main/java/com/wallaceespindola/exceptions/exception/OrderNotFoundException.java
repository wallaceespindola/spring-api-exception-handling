package com.wallaceespindola.exceptions.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends ApiException {

    public OrderNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order %d not found".formatted(id));
    }
}
