package com.wallaceespindola.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
    void validationMessages_defaultToEnglish_andHonorAcceptLanguage() {
        var body = "{\"customer\":\"\",\"amount\":1}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var english = rest.postForEntity("/api/orders", new HttpEntity<>(body, headers), String.class);
        assertThat(english.getBody()).contains("must not be blank");

        headers.set(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR");
        var portuguese = rest.postForEntity("/api/orders", new HttpEntity<>(body, headers), String.class);
        assertThat(portuguese.getBody()).doesNotContain("must not be blank");
    }

    @Test
    void actuatorHealth_isUp() {
        assertThat(rest.getForObject("/actuator/health", String.class)).contains("UP");
    }
}
