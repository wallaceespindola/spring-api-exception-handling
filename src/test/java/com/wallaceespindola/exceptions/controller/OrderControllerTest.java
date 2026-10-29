package com.wallaceespindola.exceptions.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wallaceespindola.exceptions.dto.OrderResponse;
import com.wallaceespindola.exceptions.exception.ApiExceptionHandler;
import com.wallaceespindola.exceptions.exception.OrderConflictException;
import com.wallaceespindola.exceptions.exception.OrderNotFoundException;
import com.wallaceespindola.exceptions.service.OrderService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import(ApiExceptionHandler.class)
class OrderControllerTest {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService service;

    @Test
    void health_always_returnsUpWithTimestamp() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(service.create(any())).thenReturn(new OrderResponse(1, "Ana", BigDecimal.TEN, false, Instant.now()));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer\":\"Ana\",\"amount\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void create_invalidBody_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer\":\" \",\"amount\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.field=='customer')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='amount')]").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void create_malformedJson_returns400ProblemDetail() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void get_unknownOrder_returns404ProblemDetail() throws Exception {
        when(service.get(anyLong())).thenThrow(new OrderNotFoundException(42));

        mvc.perform(get("/api/orders/42"))
                .andExpect(status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Order 42 not found"))
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/orders/42"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void get_nonNumericId_returns400() throws Exception {
        mvc.perform(get("/api/orders/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void pay_alreadyPaid_returns409() throws Exception {
        when(service.pay(anyLong())).thenThrow(new OrderConflictException(7));

        mvc.perform(post("/api/orders/7/pay"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_PAID"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void failure_unexpectedError_returns500WithoutLeakingCause() throws Exception {
        mvc.perform(get("/api/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unknownRoute_returns404ProblemDetail() throws Exception {
        mvc.perform(get("/api/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
