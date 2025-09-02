package com.apitest.apitest.twilio.service.impl;

import com.apitest.apitest.twilio.model.dto.SmsSendRequest;
import com.apitest.apitest.twilio.model.dto.SmsSendResponse;
import com.apitest.apitest.twilio.model.dto.SmsTestRequest;
import com.apitest.apitest.twilio.service.TwilioSMSService;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioSMSServiceImpl implements TwilioSMSService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.from.phone}")
    private String fromPhone;

    private volatile boolean initialized = false;

    private void initIfNecessary() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    Twilio.init(accountSid, authToken);
                    initialized = true;
                    log.debug("Initialized Twilio client with account SID ending: {}", 
                            accountSid != null && accountSid.length() > 4 ? accountSid.substring(accountSid.length() - 4) : "null");
                }
            }
        }
    }

    @Override
    public SmsSendResponse sendSms(SmsSendRequest request) {
        initIfNecessary();
        try {
            Message message = Message.creator(
                    new PhoneNumber(request.getTo()),
                    new PhoneNumber(fromPhone),
                    request.getMessage()
            ).create();

            return SmsSendResponse.builder()
                    .sid(message.getSid())
                    .status(message.getStatus() != null ? message.getStatus().toString() : null)
                    .to(message.getTo())
                    .from(message.getFrom() != null ? message.getFrom().toString() : fromPhone)
                    .errorCode(message.getErrorCode() != null ? message.getErrorCode().toString() : null)
                    .errorMessage(message.getErrorMessage())
                    .build();
        } catch (ApiException ex) {
            log.error("Failed to send SMS via Twilio: {}", ex.getMessage(), ex);
            return SmsSendResponse.builder()
                    .sid(null)
                    .status("FAILED")
                    .to(request.getTo())
                    .from(fromPhone)
                    .errorCode(String.valueOf(ex.getStatusCode()))
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    @Override
    public SmsSendResponse sendTestSms(SmsTestRequest request) {
        initIfNecessary();
        try {
            // Mirroring the sample structure: Message.creator(to, from, "test").create();
            Message message = Message.creator(
                    new PhoneNumber(request.getTo()),
                    new PhoneNumber(fromPhone),
                    request.getMessage()
            ).create();

            log.debug("Test SMS sent, SID: {}", message.getSid());

            return SmsSendResponse.builder()
                    .sid(message.getSid())
                    .status(message.getStatus() != null ? message.getStatus().toString() : null)
                    .to(message.getTo())
                    .from(message.getFrom() != null ? message.getFrom().toString() : fromPhone)
                    .errorCode(message.getErrorCode() != null ? message.getErrorCode().toString() : null)
                    .errorMessage(message.getErrorMessage())
                    .build();
        } catch (ApiException ex) {
            log.error("Failed to send TEST SMS via Twilio: {}", ex.getMessage(), ex);
            return SmsSendResponse.builder()
                    .sid(null)
                    .status("FAILED")
                    .to(request.getTo())
                    .from(fromPhone)
                    .errorCode(String.valueOf(ex.getStatusCode()))
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }
}
