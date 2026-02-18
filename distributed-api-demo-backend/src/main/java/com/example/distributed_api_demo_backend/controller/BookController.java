package com.example.distributed_api_demo_backend.controller;

import com.example.distributed_api_demo_backend.service.BookService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book/v1/hotels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book API", description = "Reservation booking and management endpoints")
public class BookController {

    private final BookService bookService;

    @PostMapping("/{hotelCode}/reservations")
    @Operation(summary = "Create reservation", description = "Create a new hotel reservation")
    public ResponseEntity<JsonNode> createReservation(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @RequestBody JsonNode request,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Create reservation request - hotel: {}, requestId: {}", hotelCode, requestId);
        log.debug("Request body: {}", request);

        JsonNode response = bookService.createReservation(request);
        
        log.info("Reservation created successfully - hotel: {}, requestId: {}", hotelCode, requestId);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{hotelCode}/reservations/{confirmationNumber}")
    @Operation(summary = "Retrieve reservation", description = "Get reservation details by confirmation number")
    public ResponseEntity<JsonNode> getReservation(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @Parameter(description = "Confirmation number", required = true)
            @PathVariable String confirmationNumber,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Retrieve reservation request - hotel: {}, confirmation: {}, requestId: {}", 
                 hotelCode, confirmationNumber, requestId);

        JsonNode response = bookService.getReservation(confirmationNumber);
        
        log.info("Reservation retrieved successfully - confirmation: {}, requestId: {}", 
                 confirmationNumber, requestId);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{hotelCode}/reservations/{confirmationNumber}")
    @Operation(summary = "Modify reservation", description = "Update an existing reservation")
    public ResponseEntity<JsonNode> modifyReservation(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @Parameter(description = "Confirmation number", required = true)
            @PathVariable String confirmationNumber,
            
            @RequestBody JsonNode request,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Modify reservation request - hotel: {}, confirmation: {}, requestId: {}", 
                 hotelCode, confirmationNumber, requestId);
        log.debug("Request body: {}", request);

        JsonNode response = bookService.modifyReservation(confirmationNumber, request);
        
        log.info("Reservation modified successfully - confirmation: {}, requestId: {}", 
                 confirmationNumber, requestId);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{hotelCode}/reservations/{confirmationNumber}")
    @Operation(summary = "Cancel reservation", description = "Cancel an existing reservation")
    public ResponseEntity<JsonNode> cancelReservation(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @Parameter(description = "Confirmation number", required = true)
            @PathVariable String confirmationNumber,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Cancel reservation request - hotel: {}, confirmation: {}, requestId: {}", 
                 hotelCode, confirmationNumber, requestId);

        JsonNode response = bookService.cancelReservation(confirmationNumber);
        
        log.info("Reservation cancelled successfully - confirmation: {}, requestId: {}", 
                 confirmationNumber, requestId);
        
        return ResponseEntity.ok(response);
    }
}
