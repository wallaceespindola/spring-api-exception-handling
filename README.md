# Spring API Exception Handling

![Fail gracefully](docs/images/banner.png)

[![Apache 2.0 License](https://img.shields.io/badge/License-Apache_2.0-blue)](LICENSE) [![Java 25](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/) [![Spring Boot 3.5](https://img.shields.io/badge/Spring_Boot-3.5-6db33f?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot) [![CI](https://github.com/wallaceespindola/spring-api-exception-handling/actions/workflows/ci.yml/badge.svg)](https://github.com/wallaceespindola/spring-api-exception-handling/actions/workflows/ci.yml)

The idea: a REST API should fail in one predictable shape. Clients should not have to guess whether an error is a bare string, a custom JSON object or an HTML page. This project is a small Spring Boot demo that sends every error, whether it comes from your code or from Spring itself, as an [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json` document with a `timestamp`.

## How it works

One `@RestControllerAdvice` (`ApiExceptionHandler`) extends `ResponseEntityExceptionHandler`, so the built-in Spring MVC exceptions (malformed JSON, wrong type in a path variable, unknown route, wrong method) already become `ProblemDetail`. The class adds three things on top:

- **Domain exceptions.** `ApiException` carries an HTTP status and a stable `code`. `OrderNotFoundException` is a 404 and `OrderConflictException` is a 409. Adding a new error means adding a small subclass, with no handler change.
- **Validation errors.** `MethodArgumentNotValidException` becomes a 400 with an `errors` array of `{field, message}`.
- **Unexpected errors.** Anything else is logged with its stack trace and returned as a 500 with a generic message, so internals never reach the client.

Every body gets a `timestamp` from a single override of `handleExceptionInternal`. `spring.mvc.problemdetails.enabled=true` is set as well, so the same format applies if you remove the advice.

## Quickstart

Requires JDK 25 and Maven 3.9+.

```bash
git clone git@github.com:wallaceespindola/spring-api-exception-handling.git
cd spring-api-exception-handling
make dev        # or: mvn spring-boot:run
```

Then try the endpoints below with `curl`, or open Swagger UI at <http://localhost:8080/swagger-ui.html>. The OpenAPI spec is at `/v3/api-docs`.

Other targets: `make test` (tests plus the JaCoCo 80% line coverage gate), `make build`, `make docker` (runs the app with Docker Compose).

## Endpoints

| Method | Path | Result |
| --- | --- | --- |
| `POST` | `/api/orders` | 201 with the order, or 400 with field errors |
| `GET` | `/api/orders/{id}` | 200, or 404 when the order does not exist, or 400 when `id` is not a number |
| `POST` | `/api/orders/{id}/pay` | 200, or 409 when already paid, or 404 |
| `GET` | `/api/failure` | Always 500 |
| `GET` | `/health` | 200 with `status` and `timestamp` (Actuator health is at `/actuator/health`) |

### 404 Not Found

```bash
curl -i localhost:8080/api/orders/42
```

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Order 42 not found",
  "instance": "/api/orders/42",
  "code": "ORDER_NOT_FOUND",
  "timestamp": "2026-10-29T19:10:56.005991Z"
}
```

### 400 Bad Request (validation)

```bash
curl -i -X POST localhost:8080/api/orders -H 'Content-Type: application/json' \
  -d '{"customer":"","amount":-5}'
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/orders",
  "errors": [
    { "field": "customer", "message": "must not be blank" },
    { "field": "amount", "message": "must be greater than 0" }
  ],
  "timestamp": "2026-10-29T19:10:56.085020Z"
}
```

Messages are in English by default. Send `Accept-Language` (for example `pt-BR`) to get them translated.

### 409 Conflict

```bash
curl -X POST localhost:8080/api/orders -H 'Content-Type: application/json' -d '{"customer":"Ana","amount":10}'
curl -X POST localhost:8080/api/orders/1/pay
curl -i -X POST localhost:8080/api/orders/1/pay
```

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Order 1 is already paid",
  "instance": "/api/orders/1/pay",
  "code": "ORDER_ALREADY_PAID",
  "timestamp": "2026-10-29T19:10:56.125267Z"
}
```

### 500 Internal Server Error

```bash
curl -i localhost:8080/api/failure
```

```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred",
  "instance": "/api/failure",
  "timestamp": "2026-10-29T19:10:56.138616Z"
}
```

The exception message thrown in the controller is only written to the log.

## Project layout

```text
src/main/java/com/wallaceespindola/exceptions/
  controller/   OrderController
  dto/          records: CreateOrderRequest, OrderResponse, HealthResponse
  exception/    ApiException, its two subclasses, ApiExceptionHandler
  service/      OrderService (in-memory store)
```

Tests are a `@WebMvcTest` that checks every error shape, a plain unit test for the service and a full-context test over a real port.

## Support

If you find this project helpful, please consider:

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-support%20my%20work-FFDD00?style=flat&labelColor=101010&logo=buy-me-a-coffee&logoColor=white)](https://www.buymeacoffee.com/wallace.espindola) [![Star on GitHub](https://img.shields.io/badge/Star%20on%20GitHub-spring--api--exception--handling-181717?style=flat&logo=github)](https://github.com/wallaceespindola/spring-api-exception-handling)

[![GitHub stars](https://img.shields.io/github/stars/wallaceespindola/spring-api-exception-handling?style=social)](https://github.com/wallaceespindola/spring-api-exception-handling/stargazers) [![GitHub forks](https://img.shields.io/github/forks/wallaceespindola/spring-api-exception-handling?style=social)](https://github.com/wallaceespindola/spring-api-exception-handling/network/members) [![GitHub watchers](https://img.shields.io/github/watchers/wallaceespindola/spring-api-exception-handling?style=social)](https://github.com/wallaceespindola/spring-api-exception-handling/watchers) [![GitHub issues](https://img.shields.io/github/issues/wallaceespindola/spring-api-exception-handling)](https://github.com/wallaceespindola/spring-api-exception-handling/issues) [![GitHub last commit](https://img.shields.io/github/last-commit/wallaceespindola/spring-api-exception-handling)](https://github.com/wallaceespindola/spring-api-exception-handling/commits/main)

Star the repo on GitHub if it helps your API work. It motivates me a lot!

## Author

Wallace Espindola, Solution Architect

- Email: <wallace.espindola@gmail.com>
- LinkedIn: <https://www.linkedin.com/in/wallaceespindola/>
- GitHub: <https://github.com/wallaceespindola/>
- Twitter/X: [@wsespindola](https://x.com/wsespindola)
- Substack: [Code & Coffee by Wallace Espindola](https://wallaceespindola.substack.com)
- Medium: <https://medium.com/@wallaceespindola>
- Dev.to: <https://dev.to/wallaceespindola>
- DZone: <https://dzone.com/users/1254611/wallacese.html>
- Speaker Deck: <https://speakerdeck.com/wallacese>
- Speaker profile: <https://sessionize.com/wallace-espindola/>
- Company: [www.wtechitsolutions.com](http://www.wtechitsolutions.com)
- Buy Me a Coffee: <https://www.buymeacoffee.com/wallace.espindola>

## License

- This project is released under the Apache 2.0 License.
- See the [LICENSE](LICENSE) file for details.
- Copyright © 2026 [Wallace Espindola](https://github.com/wallaceespindola/).
