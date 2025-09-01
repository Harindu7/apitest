package com.apitest.apitest.stripe.controller;
 
import com.apitest.apitest.stripe.service.StripeService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {
  private final StripeService stripeService;

  @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> handleWebhook(
      @RequestHeader(name = "Stripe-Signature", required = false) String signatureHeader,
      @RequestBody String payload) {
    return stripeService.handleWebhook(payload, signatureHeader);
  }
}
