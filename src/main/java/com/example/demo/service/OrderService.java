package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.exception.OrderConflictException;
import com.example.demo.exception.OrderNotFoundException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/** In-memory store, enough to trigger the 404 and 409 paths. */
@Service
public class OrderService {

    private final Map<Long, OrderResponse> orders = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public OrderResponse create(CreateOrderRequest request) {
        long id = sequence.incrementAndGet();
        OrderResponse order = new OrderResponse(id, request.customer(), request.amount(), false, Instant.now());
        orders.put(id, order);
        return order;
    }

    public OrderResponse get(long id) {
        OrderResponse order = orders.get(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }

    public OrderResponse pay(long id) {
        OrderResponse paid = orders.computeIfPresent(id, (key, order) -> {
            if (order.paid()) {
                throw new OrderConflictException(id);
            }
            return new OrderResponse(id, order.customer(), order.amount(), true, Instant.now());
        });
        if (paid == null) {
            throw new OrderNotFoundException(id);
        }
        return paid;
    }
}
