package com.apitest.apitest.mailjet.service;

import com.apitest.apitest.mailjet.model.dto.EmailRequestDTO;
import com.apitest.apitest.mailjet.model.dto.EmailResponseDTO;

public interface MailjetService {
    
    EmailResponseDTO sendEmail(EmailRequestDTO emailRequest);
    
    boolean isConfigured();
}
