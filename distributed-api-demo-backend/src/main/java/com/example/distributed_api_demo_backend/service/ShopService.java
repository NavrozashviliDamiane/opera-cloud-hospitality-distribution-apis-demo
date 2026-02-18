package com.example.distributed_api_demo_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShopService {

    private final ObjectMapper objectMapper;
    
    private JsonNode multiPropertyData;
    private JsonNode propertyOffersData;
    private JsonNode calendarData;
    private JsonNode offerDetailData;

    @PostConstruct
    public void loadTestData() {
        log.info("Loading Shop API test data...");
        try {
            multiPropertyData = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/shop-multi-property-search.json"));
            log.info("Loaded shop-multi-property-search.json");

            propertyOffersData = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/shop-property-offers.json"));
            log.info("Loaded shop-property-offers.json");

            calendarData = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/shop-calendar-availability.json"));
            log.info("Loaded shop-calendar-availability.json");

            offerDetailData = objectMapper.readTree(
                    getClass().getResourceAsStream("/data/shop-offer-detail.json"));
            log.info("Loaded shop-offer-detail.json");

            log.info("Shop API test data loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load Shop API test data", e);
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    public JsonNode searchProperties() {
        log.debug("Returning multi-property search data");
        return multiPropertyData;
    }

    public JsonNode getPropertyOffers(String hotelCode) {
        log.debug("Returning property offers for hotel: {}", hotelCode);
        return propertyOffersData;
    }

    public JsonNode getCalendarAvailability(String hotelCode) {
        log.debug("Returning calendar availability for hotel: {}", hotelCode);
        return calendarData;
    }

    public JsonNode getOfferDetail(String hotelCode, String roomType, String ratePlanCode) {
        log.debug("Returning offer detail for hotel: {}, roomType: {}, ratePlanCode: {}", 
                  hotelCode, roomType, ratePlanCode);
        return offerDetailData;
    }
}
