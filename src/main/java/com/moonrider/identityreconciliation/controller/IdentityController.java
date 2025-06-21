package com.moonrider.identityreconciliation.controller;

import com.moonrider.identityreconciliation.dto.IdentifyRequest;
import com.moonrider.identityreconciliation.dto.IdentifyResponse;
import com.moonrider.identityreconciliation.service.IdentityReconciliationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for identity reconciliation operations
 * Provides the /identify endpoint for contact consolidation
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class IdentityController {

    private final IdentityReconciliationService identityReconciliationService;

    /**
     * Identity reconciliation endpoint
     * Processes contact information and returns consolidated data
     */
    @PostMapping("/identify")
    public ResponseEntity<?> identifyContact(@Valid @RequestBody IdentifyRequest request) {
        try {
            // Validate request has at least one contact method
            if (!request.isValid()) {
                log.warn("Invalid request: no email or phone number provided");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least one of email or phoneNumber must be provided"));
            }

            log.info("Received identify request for email: {}, phone: {}",
                    request.getEmail(), request.getPhoneNumber());

            IdentifyResponse response = identityReconciliationService.identifyContact(request);

            log.info("Successfully processed identity reconciliation");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing identity reconciliation request", e);

            // Covert error handling for security (as per bonus requirements)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Service temporarily unavailable. Please try again later."));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Identity Reconciliation Service",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}