package com.wallaceespindola.exceptions.controller;

import com.wallaceespindola.exceptions.dto.CreateOrderRequest;
import com.wallaceespindola.exceptions.dto.HealthResponse;
import com.wallaceespindola.exceptions.dto.OrderResponse;
import com.wallaceespindola.exceptions.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Endpoints that trigger each kind of error response")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Liveness check")
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", Instant.now());
    }

    @Operation(summary = "Create an order (400 when the body is invalid)")
    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @Operation(summary = "Get an order (404 when it does not exist)")
    @GetMapping("/api/orders/{id}")
    public OrderResponse get(@PathVariable long id) {
        return orderService.get(id);
    }

    @Operation(summary = "Pay an order (409 when it is already paid)")
    @PostMapping("/api/orders/{id}/pay")
    public OrderResponse pay(@PathVariable long id) {
        return orderService.pay(id);
    }

    @Operation(summary = "Always fails with an unexpected error (500)")
    @GetMapping("/api/failure")
    public void failure() {
        throw new IllegalStateException("simulated bug: this message must not reach the client");
    }
}
