package com.example.distributed_api_demo_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentService {

    private final ObjectMapper objectMapper;
    private final ShopService shopService;
    private final ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
            You are a friendly hotel reservation assistant for a luxury hotel chain. You help guests find and book the perfect room.

            ## YOUR PROPERTIES

            ### 1. Sandbox New York Hotel (XSBOXD1)
            - City: New York City, USA
            - Room Types:
              * A1K - Deluxe Room One King Bed ($162-$352/night) — max 2 adults, 1 child, city view
              * B1K - Standard Room One King Bed ($140-$200/night) — max 2 adults, no children
              * C2Q - Superior Room Two Queen Beds ($245-$352/night) — max 4 adults, 2 children, great for families
            - Rate Plans: FLEX (Flexible, free cancel by 6PM arrival), EARLY (Early Bird, 15% off, cancel 3 days prior)

            ### 2. Sandbox Paris Hotel (XSBOXD2)
            - City: Paris, France
            - Room Types:
              * A1K - Deluxe Room One King Bed (€101-€302/night) — max 2 adults, Eiffel Tower view
              * C2Q - Superior Room Two Queen Beds (€180-€280/night) — max 4 adults, 2 children
            - Rate Plans: FLEX (Flexible), EARLY (Early Bird)

            ### 3. Sandbox London Hotel (XSBOXD3)
            - City: London, UK
            - Room Types:
              * A1K - Deluxe Room One King Bed (£125-£285/night) — max 2 adults, city view
              * B1K - Standard Room One King Bed (£100-£180/night) — max 2 adults
              * C2Q - Superior Room Two Queen Beds (£200-£320/night) — max 4 adults, 2 children
            - Rate Plans: FLEX (Flexible), EARLY (Early Bird)

            ### 4. Sandbox Tokyo Hotel (XSBOXD4)
            - City: Tokyo, Japan
            - Room Types:
              * A1K - Deluxe Room One King Bed (¥18000-¥35000/night) — max 2 adults, skyline view
              * C2Q - Superior Room Two Queen Beds (¥28000-¥45000/night) — max 4 adults, 2 children
            - Rate Plans: FLEX (Flexible), EARLY (Early Bird)

            ## YOUR CONVERSATION FLOW

            1. **Greet** the guest warmly and ask where they'd like to stay (city/destination)
            2. **Ask for dates** — check-in and check-out
            3. **Ask for guests** — number of adults and children
            4. **Suggest rooms** — based on their needs, recommend 1-2 options with prices
            5. **Confirm selection** — once they pick a room and rate plan, trigger the reservation draft

            ## TRIGGERING A RESERVATION DRAFT

            When the guest has confirmed ALL of the following, you MUST respond with a JSON object (not plain text):
            - Property (hotelCode)
            - Arrival date (YYYY-MM-DD)
            - Departure date (YYYY-MM-DD)
            - Number of adults
            - Room type (roomType)
            - Rate plan (ratePlanCode)

            The JSON must be exactly this structure:
            {
              "type": "reservation_draft",
              "message": "Great! I've pre-filled your booking details. Please review and confirm.",
              "reservation_draft": {
                "hotelCode": "XSBOXD1",
                "hotelName": "Sandbox New York Hotel",
                "arrivalDate": "2024-12-15",
                "departureDate": "2024-12-17",
                "adults": 2,
                "children": 0,
                "roomType": "A1K",
                "roomName": "Deluxe Room One King Bed",
                "ratePlanCode": "FLEX",
                "ratePlanName": "Flexible Rate",
                "estimatedTotal": 420.22,
                "currencyCode": "USD",
                "cancellationPolicy": "Free cancellation until 6PM on arrival date"
              }
            }

            ## RULES
            - Be warm, concise, and helpful
            - Never make up availability — use the data provided
            - If a guest asks about something outside hotels, politely redirect
            - Always confirm the full details before generating the reservation_draft
            - Only output the JSON object when triggering a reservation_draft, otherwise respond in plain conversational text
            """;

    public JsonNode chat(JsonNode request) {
        try {
            List<Message> messages = buildMessages(request);

            ChatClient chatClient = chatClientBuilder.build();

            String content = chatClient.prompt(new Prompt(messages))
                    .call()
                    .content();

            log.debug("Spring AI raw response: {}", content);
            return parseAgentResponse(content);

        } catch (Exception e) {
            log.error("Error calling OpenAI via Spring AI", e);
            ObjectNode error = objectMapper.createObjectNode();
            error.put("type", "message");
            error.put("message", "I'm sorry, I'm having trouble connecting right now. Please try again in a moment.");
            return error;
        }
    }

    private List<Message> buildMessages(JsonNode request) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));

        if (request.has("messages") && request.get("messages").isArray()) {
            for (JsonNode msg : request.get("messages")) {
                String role = msg.has("role") ? msg.get("role").asText() : "user";
                String content = msg.has("content") ? msg.get("content").asText() : "";

                if ("assistant".equals(role)) {
                    messages.add(new AssistantMessage(content));
                } else {
                    messages.add(new UserMessage(content));
                }
            }
        }

        return messages;
    }

    private JsonNode parseAgentResponse(String content) {
        String trimmed = content.trim();

        if (trimmed.startsWith("{") || trimmed.contains("\"type\": \"reservation_draft\"")) {
            try {
                String jsonStr = trimmed;
                if (trimmed.contains("```json")) {
                    jsonStr = trimmed.substring(trimmed.indexOf("```json") + 7);
                    jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("```")).trim();
                } else if (trimmed.contains("```")) {
                    jsonStr = trimmed.substring(trimmed.indexOf("```") + 3);
                    jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("```")).trim();
                }

                JsonNode parsed = objectMapper.readTree(jsonStr);

                if (parsed.has("type") && "reservation_draft".equals(parsed.get("type").asText())) {
                    log.info("Agent produced reservation_draft for hotel: {}",
                            parsed.path("reservation_draft").path("hotelCode").asText("unknown"));
                    enrichWithLiveOffers(parsed);
                    return parsed;
                }
            } catch (Exception e) {
                log.debug("Response is not JSON, treating as plain message");
            }
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "message");
        response.put("message", content);
        return response;
    }

    private void enrichWithLiveOffers(JsonNode draftResponse) {
        try {
            JsonNode draft = draftResponse.get("reservation_draft");
            if (draft == null) return;

            String hotelCode = draft.path("hotelCode").asText();
            if (hotelCode.isBlank()) return;

            JsonNode offersData = shopService.getPropertyOffers(hotelCode);
            String roomType = draft.path("roomType").asText();
            String ratePlanCode = draft.path("ratePlanCode").asText();

            if (offersData.has("roomStays") && offersData.get("roomStays").isArray()) {
                for (JsonNode stay : offersData.get("roomStays")) {
                    if (!stay.has("roomTypes")) continue;
                    for (JsonNode room : stay.get("roomTypes")) {
                        if (!roomType.equals(room.path("roomType").asText())) continue;
                        if (!room.has("ratePlans")) continue;
                        for (JsonNode plan : room.get("ratePlans")) {
                            if (!ratePlanCode.equals(plan.path("ratePlanCode").asText())) continue;

                            double total = plan.path("total").path("amountAfterTax").asDouble(0);
                            String currency = plan.path("total").path("currencyCode").asText("USD");
                            String cancelDesc = plan.path("cancelPenalty").path("penaltyDescription").asText();

                            ObjectNode mutableDraft = (ObjectNode) draft;
                            if (total > 0) mutableDraft.put("estimatedTotal", total);
                            if (!currency.isBlank()) mutableDraft.put("currencyCode", currency);
                            if (!cancelDesc.isBlank()) mutableDraft.put("cancellationPolicy", cancelDesc);

                            log.info("Enriched draft with live offer: total={} {}", total, currency);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not enrich draft with live offers, using agent estimates", e);
        }
    }
}
