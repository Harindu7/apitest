package com.apitest.apitest.stripe.service.impl;

import com.apitest.apitest.stripe.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    // Optional: set your API key if you also plan to call Stripe APIs from the server
    @Value("${stripe.api.key:}")
    private String apiKey;

    @Override
    public ResponseEntity<String> handleWebhook(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("Stripe webhook secret is not configured. Set 'STRIPE_WEBHOOK_SECRET' env var or 'stripe.webhook.secret' property.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook not configured");
        }

        try {
            if (apiKey != null && !apiKey.isBlank()) {
                Stripe.apiKey = apiKey;
            }

            if (signatureHeader == null || signatureHeader.isBlank()) {
                log.warn("Missing Stripe-Signature header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature header");
            }

            // Verify signature and parse the event
            Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

            // Handle event types you care about
            String eventType = event.getType();
            log.info("Received Stripe event: {}", eventType);

            switch (eventType) {
                case "customer.subscription.created" -> {
                    log.info("customer.subscription.created: {}", event.getDataObjectDeserializer().getObject().orElse(null));
                    // TODO: add your business logic
                }
                case "charge.refunded" -> {
                    log.info("Charge refunded: {}", event.getDataObjectDeserializer().getObject().orElse(null));
                }
                default -> log.debug("Unhandled event type: {}", eventType);
            }

            return ResponseEntity.ok("success");
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error handling Stripe webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook error");
        }
    }
}
