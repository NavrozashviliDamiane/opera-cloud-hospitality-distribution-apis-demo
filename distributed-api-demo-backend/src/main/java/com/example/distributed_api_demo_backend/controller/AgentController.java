package com.example.distributed_api_demo_backend.controller;

import com.example.distributed_api_demo_backend.service.AgentService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservation Agent", description = "AI-powered conversational reservation assistant")
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/reservation-agent")
    @Operation(
        summary = "Chat with reservation agent",
        description = "Send a conversation history to the AI agent. Returns either a chat message or a structured reservation_draft when enough info is collected."
    )
    public ResponseEntity<JsonNode> chat(@RequestBody JsonNode request) {
        log.info("Agent chat request received");

        JsonNode response = agentService.chat(request);

        log.info("Agent chat response type: {}",
                response.has("reservation_draft") ? "reservation_draft" : "message");

        return ResponseEntity.ok(response);
    }
}
