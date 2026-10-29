package com.wallaceespindola.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ExceptionHandlerAppTest {

    @Autowired
    private RestTestClient rest;

    @Test
    void getOrder_missing_returnsProblemJsonEndToEnd() {
        rest.get().uri("/api/orders/999").exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("ORDER_NOT_FOUND", "timestamp"));
    }

    @Test
    void validationMessages_defaultToEnglish_andHonorAcceptLanguage() {
        var body = "{\"customer\":\"\",\"amount\":1}";

        rest.post().uri("/api/orders").contentType(MediaType.APPLICATION_JSON).body(body).exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(english -> assertThat(english).contains("must not be blank"));

        rest.post().uri("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR").body(body).exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(portuguese -> assertThat(portuguese).doesNotContain("must not be blank"));
    }

    @Test
    void actuatorHealth_isUp() {
        rest.get().uri("/actuator/health").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("UP"));
    }
}
