package com.apitest.apitest.mailjet.service.impl;

import com.apitest.apitest.mailjet.model.dto.EmailRequestDTO;
import com.apitest.apitest.mailjet.model.dto.EmailResponseDTO;
import com.apitest.apitest.mailjet.service.MailjetService;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailjetServiceimpl implements MailjetService {
    
    private static final Logger logger = LoggerFactory.getLogger(MailjetServiceimpl.class);
    
    @Value("${mailjet.api.key}")
    private String apiKey;
    
    @Value("${mailjet.api.secret}")
    private String apiSecret;
    
    @Value("${mailjet.from.email}")
    private String fromEmail;
    
    @Value("${mailjet.from.name:API Test}")
    private String fromName;
    
    @Override
    public EmailResponseDTO sendEmail(EmailRequestDTO emailRequest) {
        try {
            logger.info("Attempting to send email to: {}", emailRequest.getTo());
            
            // Create Mailjet client
            MailjetClient client = new MailjetClient(
                ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(apiSecret)
                    .build()
            );
            
            // Build email request
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                    .put(new JSONObject()
                        .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", fromEmail)
                            .put("Name", fromName))
                        .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                .put("Email", emailRequest.getTo())))
                        .put(Emailv31.Message.SUBJECT, emailRequest.getSubject())
                        .put(Emailv31.Message.TEXTPART, emailRequest.getBody())
                        .put(Emailv31.Message.HTMLPART, 
                            "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                            emailRequest.getBody().replace("\n", "<br>") +
                            "</div>")));
            
            // Send email
            MailjetResponse response = client.post(request);
            
            if (response.getStatus() == 200) {
                JSONArray messages = response.getData();
                if (messages.length() > 0) {
                    JSONObject message = messages.getJSONObject(0);
                    String messageId = message.optString("MessageID", "unknown");
                    
                    logger.info("Email sent successfully. Message ID: {}", messageId);
                    return EmailResponseDTO.success(messageId);
                } else {
                    logger.error("No message data in response");
                    return EmailResponseDTO.error("No message data in response");
                }
            } else {
                String errorMessage = "Failed to send email. Status: " + response.getStatus() + 
                                    ", Data: " + response.getData();
                logger.error(errorMessage);
                return EmailResponseDTO.error(errorMessage);
            }
            
        } catch (MailjetException e) {
            String errorMessage = "Mailjet API error: " + e.getMessage();
            logger.error(errorMessage, e);
            return EmailResponseDTO.error(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while sending email: " + e.getMessage();
            logger.error(errorMessage, e);
            return EmailResponseDTO.error(errorMessage);
        }
    }
    
    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && 
               apiSecret != null && !apiSecret.isEmpty() &&
               fromEmail != null && !fromEmail.isEmpty();
    }
}
