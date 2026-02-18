package com.example.distributed_api_demo_backend.controller;

import com.example.distributed_api_demo_backend.service.ShopService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop/v1/hotels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shop API", description = "Hotel shopping and availability endpoints")
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    @Operation(summary = "Multi-property search", description = "Search for availability across multiple properties")
    public ResponseEntity<JsonNode> searchProperties(
            @Parameter(description = "Number of adults", required = true)
            @RequestParam Integer adults,
            
            @Parameter(description = "Number of rooms", required = true)
            @RequestParam Integer numberOfUnits,
            
            @Parameter(description = "Arrival date (YYYY-MM-DD)", required = true)
            @RequestParam String arrivalDate,
            
            @Parameter(description = "Departure date (YYYY-MM-DD)", required = true)
            @RequestParam String departureDate,
            
            @Parameter(description = "Hotel chain code")
            @RequestParam(required = false) String chainCode,
            
            @Parameter(description = "Comma-separated hotel codes")
            @RequestParam(required = false) String hotelCodes,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Property search request - adults: {}, units: {}, arrival: {}, departure: {}, requestId: {}", 
                 adults, numberOfUnits, arrivalDate, departureDate, requestId);

        JsonNode response = shopService.searchProperties();
        
        log.info("Returning {} properties, requestId: {}", 
                 response.get("roomStays").size(), requestId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hotelCode}/offers")
    @Operation(summary = "Get property offers", description = "Get detailed offers with room types and rate plans for a specific property")
    public ResponseEntity<JsonNode> getPropertyOffers(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @Parameter(description = "Number of adults", required = true)
            @RequestParam Integer adults,
            
            @Parameter(description = "Number of rooms", required = true)
            @RequestParam Integer numberOfUnits,
            
            @Parameter(description = "Arrival date (YYYY-MM-DD)", required = true)
            @RequestParam String arrivalDate,
            
            @Parameter(description = "Departure date (YYYY-MM-DD)", required = true)
            @RequestParam String departureDate,
            
            @Parameter(description = "Comma-separated rate plan codes")
            @RequestParam(required = false) String ratePlanCodes,
            
            @Parameter(description = "Match rate plan codes only")
            @RequestParam(required = false) Boolean ratePlanCodeMatchOnly,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Property offers request - hotel: {}, adults: {}, units: {}, arrival: {}, departure: {}, requestId: {}", 
                 hotelCode, adults, numberOfUnits, arrivalDate, departureDate, requestId);

        JsonNode response = shopService.getPropertyOffers(hotelCode);
        
        log.info("Returning offers for hotel: {}, requestId: {}", hotelCode, requestId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hotelCode}/calendar")
    @Operation(summary = "Get calendar availability", description = "Get calendar view of availability for a date range")
    public ResponseEntity<JsonNode> getCalendarAvailability(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @Parameter(description = "Number of adults", required = true)
            @RequestParam Integer adults,
            
            @Parameter(description = "Number of rooms", required = true)
            @RequestParam Integer numberOfUnits,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam String startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam String endDate,
            
            @Parameter(description = "Length of stay in nights")
            @RequestParam(required = false) Integer lengthOfStay,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Calendar availability request - hotel: {}, adults: {}, units: {}, start: {}, end: {}, requestId: {}", 
                 hotelCode, adults, numberOfUnits, startDate, endDate, requestId);

        JsonNode response = shopService.getCalendarAvailability(hotelCode);
        
        log.info("Returning calendar for hotel: {}, requestId: {}", hotelCode, requestId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hotelCode}/offer")
    @Operation(summary = "Get single offer detail", description = "Retrieve a single offer by room type and rate plan code")
    public ResponseEntity<JsonNode> getOfferDetail(
            @Parameter(description = "Hotel code", required = true)
            @PathVariable String hotelCode,
            
            @Parameter(description = "Room type code", required = true)
            @RequestParam String roomType,
            
            @Parameter(description = "Rate plan code", required = true)
            @RequestParam String ratePlanCode,
            
            @Parameter(description = "Number of adults", required = true)
            @RequestParam Integer adults,
            
            @Parameter(description = "Number of rooms", required = true)
            @RequestParam Integer numberOfUnits,
            
            @Parameter(description = "Arrival date (YYYY-MM-DD)", required = true)
            @RequestParam String arrivalDate,
            
            @Parameter(description = "Departure date (YYYY-MM-DD)", required = true)
            @RequestParam String departureDate,
            
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("x-app-key") String appKey,
            @RequestHeader("x-channelCode") String channelCode,
            @RequestHeader("x-request-id") String requestId) {

        log.info("Offer detail request - hotel: {}, roomType: {}, ratePlanCode: {}, adults: {}, units: {}, arrival: {}, departure: {}, requestId: {}", 
                 hotelCode, roomType, ratePlanCode, adults, numberOfUnits, arrivalDate, departureDate, requestId);

        JsonNode response = shopService.getOfferDetail(hotelCode, roomType, ratePlanCode);
        
        log.info("Returning offer detail for hotel: {}, roomType: {}, ratePlanCode: {}, requestId: {}", 
                 hotelCode, roomType, ratePlanCode, requestId);
        
        return ResponseEntity.ok(response);
    }
}
