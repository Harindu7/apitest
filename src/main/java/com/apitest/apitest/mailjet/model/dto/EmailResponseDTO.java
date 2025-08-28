package com.apitest.apitest.mailjet.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailResponseDTO {

    private boolean success;
    private String message;
    private String messageId;
    private long timestamp;

    // Constructor for success response
    public EmailResponseDTO(boolean success, String message, String messageId) {
        this.success = success;
        this.message = message;
        this.messageId = messageId;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor for error response
    public EmailResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Static factory methods
    public static EmailResponseDTO success(String messageId) {
        return new EmailResponseDTO(true, "Email sent successfully", messageId);
    }

    public static EmailResponseDTO error(String errorMessage) {
        return new EmailResponseDTO(false, errorMessage);
    }

    // Custom setter to automatically set timestamp
    public void setSuccess(boolean success) {
        this.success = success;
        if (this.timestamp == 0) {
            this.timestamp = System.currentTimeMillis();
        }
    }
}