package com.apitest.apitest.openapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.apitest.apitest.openapi.model.dto.OpenAiChatRequest;
import com.apitest.apitest.openapi.model.dto.OpenAiChatResponse;
import com.apitest.apitest.openapi.model.dto.OpenAiResponseRequest;
import com.apitest.apitest.openapi.model.dto.OpenAiResponseResult;
import com.apitest.apitest.openapi.service.OpenAiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/openai")
@RequiredArgsConstructor
@Validated
public class OpenApiController {

    private final OpenAiService openAiService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Validated @RequestBody OpenAiChatRequest request) {
        try {
            OpenAiChatResponse response = openAiService.createChatCompletion(request);
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                return ResponseEntity.status(429).body(new ErrorBody(429, "Rate limited by OpenAI. Please retry later."));
            }
            return ResponseEntity.status(e.getStatusCode().value()).body(new ErrorBody(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    @PostMapping("/respond")
    public ResponseEntity<?> respond(@Validated @RequestBody OpenAiResponseRequest request) {
        try {
            OpenAiResponseResult result = openAiService.createResponse(request);
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                return ResponseEntity.status(429).body(new ErrorBody(429, "Rate limited by OpenAI. Please retry later."));
            }
            return ResponseEntity.status(e.getStatusCode().value()).body(new ErrorBody(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    record ErrorBody(int status, String message) {}
}
