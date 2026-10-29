package com.wallaceespindola.exceptions.dto;

import java.time.Instant;

public record HealthResponse(String status, Instant timestamp) {
}
