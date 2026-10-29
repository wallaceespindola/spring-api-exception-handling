package com.wallaceespindola.exceptions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateOrderRequest(@NotBlank String customer, @NotNull @Positive BigDecimal amount) {
}
