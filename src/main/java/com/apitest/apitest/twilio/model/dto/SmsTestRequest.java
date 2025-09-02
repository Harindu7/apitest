package com.apitest.apitest.twilio.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsTestRequest {
    @NotBlank(message = "Destination phone number is required")
    private String to;

    @NotBlank(message = "Message body is required")
    private String message;
}
