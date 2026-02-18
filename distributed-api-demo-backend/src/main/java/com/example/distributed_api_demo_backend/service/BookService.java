package com.example.distributed_api_demo_backend.service;

import com.example.distributed_api_demo_backend.exception.NoAvailabilityException;
import com.example.distributed_api_demo_backend.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {

    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    
    private final Map<String, JsonNode> reservations = new ConcurrentHashMap<>();
    
    private JsonNode successTemplate;
    private JsonNode ccGuaranteedTemplate;
    private JsonNode retrieveTemplate;
    private JsonNode cancellationTemplate;

    @PostConstruct
    public void loadTestData() {
        log.info("Loading Book API test data...");
        try {
            successTemplate = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/book-create-reservation-success.json"));
            log.info("Loaded book-create-reservation-success.json");

            ccGuaranteedTemplate = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/book-create-reservation-cc-guaranteed.json"));
            log.info("Loaded book-create-reservation-cc-guaranteed.json");

            retrieveTemplate = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/book-retrieve-reservation.json"));
            log.info("Loaded book-retrieve-reservation.json");

            cancellationTemplate = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/book-cancel-reservation.json"));
            log.info("Loaded book-cancel-reservation.json");

            log.info("Book API test data loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load Book API test data", e);
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    public JsonNode createReservation(JsonNode request) {
        log.debug("Creating reservation with request: {}", request);

        if (random.nextDouble() < 0.1) {
            log.warn("Simulating no availability (10% chance)");
            throw new NoAvailabilityException("No availability for requested dates");
        }

        boolean hasCreditCard = checkForCreditCard(request);
        JsonNode template = hasCreditCard ? ccGuaranteedTemplate : successTemplate;
        
        String confirmationNumber = generateConfirmationNumber();
        log.info("Generated confirmation number: {}", confirmationNumber);

        JsonNode response = template.deepCopy();
        updateConfirmationNumber(response, confirmationNumber);
        updateTimestamp(response);

        reservations.put(confirmationNumber, response);
        log.info("Reservation created successfully with confirmation: {}", confirmationNumber);

        return response;
    }

    public JsonNode getReservation(String confirmationNumber) {
        log.debug("Retrieving reservation: {}", confirmationNumber);
        
        JsonNode reservation = reservations.get(confirmationNumber);
        if (reservation == null) {
            log.warn("Reservation not found: {}", confirmationNumber);
            throw new NotFoundException("Reservation not found: " + confirmationNumber);
        }
        
        log.info("Reservation retrieved successfully: {}", confirmationNumber);
        return reservation;
    }

    public JsonNode modifyReservation(String confirmationNumber, JsonNode request) {
        log.debug("Modifying reservation: {}", confirmationNumber);
        
        if (!reservations.containsKey(confirmationNumber)) {
            log.warn("Reservation not found for modification: {}", confirmationNumber);
            throw new NotFoundException("Reservation not found: " + confirmationNumber);
        }

        JsonNode existingReservation = reservations.get(confirmationNumber);
        JsonNode modifiedReservation = existingReservation.deepCopy();
        updateTimestamp(modifiedReservation);

        reservations.put(confirmationNumber, modifiedReservation);
        log.info("Reservation modified successfully: {}", confirmationNumber);

        return modifiedReservation;
    }

    public JsonNode cancelReservation(String confirmationNumber) {
        log.debug("Cancelling reservation: {}", confirmationNumber);
        
        if (!reservations.containsKey(confirmationNumber)) {
            log.warn("Reservation not found for cancellation: {}", confirmationNumber);
            throw new NotFoundException("Reservation not found: " + confirmationNumber);
        }

        reservations.remove(confirmationNumber);
        
        JsonNode response = cancellationTemplate.deepCopy();
        updateConfirmationNumber(response, confirmationNumber);
        updateTimestamp(response);
        updateCancellationDate(response);

        log.info("Reservation cancelled successfully: {}", confirmationNumber);
        return response;
    }

    private boolean checkForCreditCard(JsonNode request) {
        if (request.has("reservations") && request.get("reservations").isArray()) {
            ArrayNode reservations = (ArrayNode) request.get("reservations");
            if (reservations.size() > 0) {
                JsonNode reservation = reservations.get(0);
                if (reservation.has("roomStay")) {
                    JsonNode roomStay = reservation.get("roomStay");
                    if (roomStay.has("guarantee")) {
                        JsonNode guarantee = roomStay.get("guarantee");
                        return guarantee.has("creditCard");
                    }
                }
            }
        }
        return false;
    }

    private String generateConfirmationNumber() {
        return String.format("%07d", random.nextInt(10000000));
    }

    private void updateConfirmationNumber(JsonNode response, String confirmationNumber) {
        if (response.isArray() && response.size() > 0) {
            ObjectNode reservation = (ObjectNode) response.get(0);
            if (reservation.has("reservationIds") && reservation.get("reservationIds").isArray()) {
                ArrayNode ids = (ArrayNode) reservation.get("reservationIds");
                for (JsonNode idNode : ids) {
                    if (idNode.has("type") && "Confirmation".equals(idNode.get("type").asText())) {
                        ((ObjectNode) idNode).put("id", confirmationNumber);
                    }
                }
            }
        }
    }

    private void updateTimestamp(JsonNode response) {
        if (response.isArray() && response.size() > 0) {
            ObjectNode reservation = (ObjectNode) response.get(0);
            reservation.put("lastModifyDateTime", Instant.now().toString());
        }
    }

    private void updateCancellationDate(JsonNode response) {
        if (response.isArray() && response.size() > 0) {
            ObjectNode reservation = (ObjectNode) response.get(0);
            if (reservation.has("roomStay")) {
                ObjectNode roomStay = (ObjectNode) reservation.get("roomStay");
                roomStay.put("cancellationDate", Instant.now().toString());
            }
        }
    }
}
