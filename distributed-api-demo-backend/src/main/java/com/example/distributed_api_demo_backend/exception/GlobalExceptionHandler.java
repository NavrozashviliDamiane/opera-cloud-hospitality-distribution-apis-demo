package com.example.distributed_api_demo_backend.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<JsonNode> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(404, "Resource not found", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NoAvailabilityException.class)
    public ResponseEntity<JsonNode> handleNoAvailabilityException(NoAvailabilityException ex, HttpServletRequest request) {
        log.error("No availability: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(409, "No availability", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<JsonNode> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.error("Bad request on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, "Invalid request parameters", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("No resource found: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonNode> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500, "Internal server error", ex.getMessage(), request.getRequestURI()));
    }

    private JsonNode buildError(int status, String title, String detail, String path) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("status", status);
        error.put("title", title);
        error.put("detail", detail);
        error.put("o:errorPath", path);
        error.put("timestamp", Instant.now().toString());
        return error;
    }
}
