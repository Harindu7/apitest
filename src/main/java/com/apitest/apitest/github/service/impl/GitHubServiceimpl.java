package com.apitest.apitest.github.service.impl;

import com.apitest.apitest.github.model.dto.GitHubAuthResponse;
import com.apitest.apitest.github.model.dto.GitHubBranch;
import com.apitest.apitest.github.model.dto.GitHubRepository;
import com.apitest.apitest.github.service.GitHubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubServiceimpl implements GitHubService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    @Override
    public Mono<GitHubAuthResponse> getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);

        return webClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        log.info("GitHub OAuth response: {}", response);
                        return Mono.just(objectMapper.readValue(response, GitHubAuthResponse.class));
                    } catch (Exception e) {
                        log.error("Error parsing GitHub response: {}", e.getMessage());
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<String> getGitHubAuthorizeUrl() {
        String state = UUID.randomUUID().toString();
        String authorizeUrl = "https://github.com/login/oauth/authorize?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=repo,user" +
                "&state=" + state;
        return Mono.just(authorizeUrl);
    }

    @Override
    public Flux<GitHubRepository> getUserRepositories(String token) {
        return webClient.get()
                .uri("https://api.github.com/user/repos")
                .header(HttpHeaders.AUTHORIZATION, "token " + token)
                .retrieve()
                .bodyToFlux(GitHubRepository.class);
    }

    @Override
    public Flux<GitHubBranch> getRepositoryBranches(String token, String owner, String repo) {
        return webClient.get()
                .uri(String.format("https://api.github.com/repos/%s/%s/branches", owner, repo))
                .header(HttpHeaders.AUTHORIZATION, "token " + token)
                .retrieve()
                .bodyToFlux(GitHubBranch.class);
    }

}
