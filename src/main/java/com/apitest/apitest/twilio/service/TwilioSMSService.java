package com.apitest.apitest.twilio.service;

import com.apitest.apitest.twilio.model.dto.SmsSendRequest;
import com.apitest.apitest.twilio.model.dto.SmsSendResponse;
import com.apitest.apitest.twilio.model.dto.SmsTestRequest;

public interface TwilioSMSService {
    SmsSendResponse sendSms(SmsSendRequest request);
    SmsSendResponse sendTestSms(SmsTestRequest request);
}
