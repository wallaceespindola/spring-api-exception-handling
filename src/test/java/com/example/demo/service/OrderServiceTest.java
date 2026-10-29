package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.exception.OrderConflictException;
import com.example.demo.exception.OrderNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private final OrderService service = new OrderService();

    private OrderResponse newOrder() {
        return service.create(new CreateOrderRequest("Ana", BigDecimal.TEN));
    }

    @Test
    void create_validRequest_returnsUnpaidOrderWithId() {
        OrderResponse order = newOrder();

        assertThat(order.id()).isEqualTo(1);
        assertThat(order.paid()).isFalse();
        assertThat(service.get(order.id())).isEqualTo(order);
    }

    @Test
    void get_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> service.get(99)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void pay_unpaidOrder_marksPaid() {
        assertThat(service.pay(newOrder().id()).paid()).isTrue();
    }

    @Test
    void pay_alreadyPaid_throwsConflict() {
        long id = newOrder().id();
        service.pay(id);

        assertThatThrownBy(() -> service.pay(id)).isInstanceOf(OrderConflictException.class);
    }

    @Test
    void pay_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> service.pay(99)).isInstanceOf(OrderNotFoundException.class);
    }
}
