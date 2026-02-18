package com.example.distributed_api_demo_backend.exception;

public class NoAvailabilityException extends RuntimeException {
    public NoAvailabilityException(String message) {
        super(message);
    }
}
