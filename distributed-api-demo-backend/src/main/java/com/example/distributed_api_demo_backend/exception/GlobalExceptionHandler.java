package com.example.distributed_api_demo_backend.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<JsonNode> handleNotFoundException(NotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        try {
            JsonNode errorResponse = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/error-404-not-found.json"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createDefaultError(404, ex.getMessage()));
        }
    }

    @ExceptionHandler(NoAvailabilityException.class)
    public ResponseEntity<JsonNode> handleNoAvailabilityException(NoAvailabilityException ex) {
        log.error("No availability: {}", ex.getMessage());
        try {
            JsonNode errorResponse = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/error-409-no-availability.json"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createDefaultError(409, ex.getMessage()));
        }
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<JsonNode> handleBadRequest(Exception ex) {
        log.error("Bad request: {}", ex.getMessage());
        try {
            JsonNode errorResponse = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/error-400-bad-request.json"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createDefaultError(400, ex.getMessage()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonNode> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createDefaultError(500, "Internal server error"));
    }

    private JsonNode createDefaultError(int status, String message) {
        return objectMapper.createObjectNode()
                .put("status", status)
                .put("title", message)
                .put("timestamp", Instant.now().toString());
    }
}
