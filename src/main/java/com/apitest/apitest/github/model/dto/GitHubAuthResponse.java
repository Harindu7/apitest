package com.apitest.apitest.github.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    private String scope;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("error_description")
    private String errorDescription;
    
    @JsonProperty("error_uri")
    private String errorUri;
    
    public boolean hasError() {
        return error != null || errorDescription != null;
    }
}
