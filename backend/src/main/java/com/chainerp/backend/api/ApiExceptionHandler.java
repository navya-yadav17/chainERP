package com.chainerp.backend.api;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        String code = status.is4xxClientError() ? "CLIENT_ERROR" : "SERVER_ERROR";
        return ResponseEntity.status(status).body(new ApiErrorResponse(
            new ApiError(code, exception.getReason(), Map.of())
        ));
    }

    public record ApiErrorResponse(ApiError error) {
    }

    public record ApiError(String code, String message, Map<String, Object> details) {
    }
}
