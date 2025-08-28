package com.apitest.apitest.mailjet.model.dto;

public class EmailResponseDTO {
    
    private boolean success;
    private String message;
    private String messageId;
    private long timestamp;
    
    // Default constructor
    public EmailResponseDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
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
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "EmailResponseDTO{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
