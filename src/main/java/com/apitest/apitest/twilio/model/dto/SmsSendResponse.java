package com.apitest.apitest.twilio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendResponse {
    private String sid;
    private String status;
    private String to;
    private String from;
    private String errorCode;
    private String errorMessage;
}
