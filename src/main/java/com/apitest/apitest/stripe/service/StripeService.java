package com.apitest.apitest.stripe.service;

import org.springframework.http.ResponseEntity;

public interface StripeService {
    ResponseEntity<String> handleWebhook(String payload, String signatureHeader);
}
