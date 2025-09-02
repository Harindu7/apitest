package com.apitest.apitest.twilio.controller;

import com.apitest.apitest.twilio.model.dto.SmsSendRequest;
import com.apitest.apitest.twilio.model.dto.SmsSendResponse;
import com.apitest.apitest.twilio.model.dto.SmsTestRequest;
import com.apitest.apitest.twilio.service.TwilioSMSService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/twilio")
@RequiredArgsConstructor
public class TwilioSMSController {

    private final TwilioSMSService twilioSMSService;

    @PostMapping("/sms/send")
    public ResponseEntity<SmsSendResponse> sendSms(@Valid @RequestBody SmsSendRequest request) {
        SmsSendResponse response = twilioSMSService.sendSms(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sms/test")
    public ResponseEntity<SmsSendResponse> sendTestSms(@Valid @RequestBody SmsTestRequest request) {
        SmsSendResponse response = twilioSMSService.sendTestSms(request);
        return ResponseEntity.ok(response);
    }
}
