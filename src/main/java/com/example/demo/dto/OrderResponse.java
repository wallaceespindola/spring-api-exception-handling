package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(long id, String customer, BigDecimal amount, boolean paid, Instant timestamp) {
}
