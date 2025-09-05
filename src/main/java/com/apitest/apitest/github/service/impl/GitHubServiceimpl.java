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

    @Override
    public Mono<Void> createWebhook(String token, String owner, String repo, String callbackUrl, String secret) {
        return webClient.post()
                .uri(String.format("https://api.github.com/repos/%s/%s/hooks", owner, repo))
                .header(HttpHeaders.AUTHORIZATION, "token " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new java.util.HashMap<String, Object>() {{
                    put("name", "web");
                    put("active", true);
                    put("events", java.util.Arrays.asList("push", "pull_request"));
                    put("config", new java.util.HashMap<String, Object>() {{
                        put("url", callbackUrl);
                        put("content_type", "json");
                        put("secret", secret);
                        put("insecure_ssl", "0");
                    }});
                }}))
                .retrieve()
                .onStatus(httpStatus -> httpStatus.isError(), clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("Failed to create webhook: body={}", body);
                            return Mono.error(new IllegalStateException("GitHub webhook creation failed: " + body));
                        }))
                .bodyToMono(String.class)
                .then();
    }

    @Override
    public Mono<String> getLatestCommitShaForPath(String token, String owner, String repo, String branch, String path) {
        String branchRef = branch.startsWith("refs/") ? branch : ("refs/heads/" + branch);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.github.com")
                        .path(String.format("/repos/%s/%s/commits", owner, repo))
                        .queryParam("sha", branchRef)
                        .queryParam("path", path)
                        .queryParam("per_page", 1)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "token " + token)
                .retrieve()
                .bodyToFlux(Object.class)
                .next()
                .map(obj -> objectMapper.convertValue(obj, java.util.Map.class))
                .map(map -> (String) map.get("sha"));
    }

    @Override
    public Flux<String> getAddedLinesInCommitForPath(String token, String owner, String repo, String commitSha, String path) {
        return webClient.get()
                .uri(String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, commitSha))
                .header(HttpHeaders.AUTHORIZATION, "token " + token)
                .retrieve()
                .bodyToMono(Object.class)
                .flatMapMany(obj -> {
                    java.util.Map<?, ?> commit = objectMapper.convertValue(obj, java.util.Map.class);
                    java.util.List<?> files = (java.util.List<?>) commit.get("files");
                    if (files == null) return Flux.empty();
                    return Flux.fromIterable(files)
                            .map(fileObj -> objectMapper.convertValue(fileObj, java.util.Map.class))
                            .filter(file -> path.equals(file.get("filename")))
                            .flatMap(file -> {
                                String patch = (String) file.get("patch");
                                if (patch == null) return Flux.empty();
                                return Flux.fromStream(patch.lines())
                                        .filter(line -> line.startsWith("+") && !line.startsWith("+++"))
                                        .map(line -> line.substring(1));
                            });
                });
    }

}
