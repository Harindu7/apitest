package com.apitest.apitest.mailjet.controller;

import com.apitest.apitest.mailjet.model.dto.EmailRequestDTO;
import com.apitest.apitest.mailjet.model.dto.EmailResponseDTO;
import com.apitest.apitest.mailjet.service.MailjetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    
    @Autowired
    private MailjetService mailjetService;
    
    @PostMapping("/send")
    public ResponseEntity<EmailResponseDTO> sendEmail(@Valid @RequestBody EmailRequestDTO emailRequest) {
        try {
            logger.info("Received email send request: {}", emailRequest);
            
            // Check if Mailjet is configured
            if (!mailjetService.isConfigured()) {
                logger.error("Mailjet service is not properly configured");
                EmailResponseDTO errorResponse = EmailResponseDTO.error(
                    "Email service is not configured. Please check your Mailjet API credentials.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }
            
            // Send email
            EmailResponseDTO response = mailjetService.sendEmail(emailRequest);
            
            if (response.isSuccess()) {
                logger.info("Email sent successfully to: {}", emailRequest.getTo());
                return ResponseEntity.ok(response);
            } else {
                logger.error("Failed to send email: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error in email controller", e);
            EmailResponseDTO errorResponse = EmailResponseDTO.error(
                "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        if (mailjetService.isConfigured()) {
            return ResponseEntity.ok("Email service is configured and ready");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Email service is not configured");
        }
    }
}
