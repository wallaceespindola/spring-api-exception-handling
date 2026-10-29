package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExceptionHandlerAppTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void getOrder_missing_returnsProblemJsonEndToEnd() {
        var response = rest.getForEntity("/api/orders/999", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getHeaders().getContentType().toString()).startsWith("application/problem+json");
        assertThat(response.getBody()).contains("ORDER_NOT_FOUND", "timestamp");
    }

    @Test
    void actuatorHealth_isUp() {
        assertThat(rest.getForObject("/actuator/health", String.class)).contains("UP");
    }
}
