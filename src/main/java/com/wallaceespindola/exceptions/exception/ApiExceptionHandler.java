package com.wallaceespindola.exceptions.exception;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps every error to an RFC 9457 {@link ProblemDetail}. Extending {@link ResponseEntityExceptionHandler} covers the
 * built-in Spring MVC exceptions (400 malformed body, 404 unknown route, 405, 415...); this class adds our own.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final String TIMESTAMP = "timestamp";
    static final String CODE = "code";
    static final String ERRORS = "errors";

    public record FieldError(String field, String message) {
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {
        log.warn("Domain error {}: {}", ex.getCode(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setProperty(CODE, ex.getCode());
        return handleExceptionInternal(ex, problem, new HttpHeaders(), ex.getStatus(), request);
    }

    /** Last resort: log the cause, never leak it to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        ex.getBody().setDetail("Validation failed");
        ex.getBody().setProperty(ERRORS, errors);
        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }

    /** Single exit point for every handler above and for the inherited ones, so all bodies get a timestamp. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        // inherited handlers pass a null body and let the parent build it from the exception
        Object resolved = body == null && ex instanceof ErrorResponse errorResponse ? errorResponse.getBody() : body;
        if (resolved instanceof ProblemDetail problem) {
            problem.setProperty(TIMESTAMP, Instant.now());
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
}
