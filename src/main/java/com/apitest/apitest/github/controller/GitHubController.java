package com.apitest.apitest.github.controller;

import com.apitest.apitest.github.model.dto.*;
import com.apitest.apitest.github.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Tag(name = "GitHub", description = "GitHub Integration APIs")
@SecurityRequirement(name = "bearerAuth")
public class GitHubController {

    private final GitHubService gitHubService;

    @GetMapping("/login")
    @Operation(summary = "Redirect to GitHub for OAuth2 login")
    public ResponseEntity<Void> login() {
        String authorizeUrl = gitHubService.getGitHubAuthorizeUrl().block(); // Block to get the URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizeUrl))
                .build();
    }

    @GetMapping("/login/oauth2/code/github")
    @Operation(summary = "Handle GitHub OAuth2 callback")
    public Mono<ResponseEntity<?>> handleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state) {
        
        log.info("Received OAuth callback with code: {}", code);
        
        return gitHubService.getAccessToken(code)
                .flatMap(authResponse -> {
                    if (authResponse.hasError()) {
                        log.error("GitHub OAuth error: {}", authResponse.getErrorDescription());
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                        "error", "GitHub authentication failed",
                                        "details", authResponse.getErrorDescription()
                                )));
                    }
                    return Mono.just(ResponseEntity.ok(authResponse));
                })
                .onErrorResume(e -> {
                    log.error("Error during GitHub OAuth: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of(
                                    "error", "Failed to authenticate with GitHub",
                                    "details", e.getMessage()
                            )));
                });
    }

    @GetMapping("/repositories")
    @Operation(summary = "Get authenticated user's repositories")
    public Mono<ResponseEntity<Object>> getRepositories(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header")));
        }

        String authToken = authHeader.substring(7); // Remove "Bearer " prefix

        return gitHubService.getUserRepositories(authToken)
                .collectList()
                .map(repos -> ResponseEntity.ok().body((Object) repos))
                .onErrorResume(e -> {
                    log.error("Error fetching repositories: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body((Object) Map.of(
                                    "error", "Failed to fetch repositories",
                                    "details", e.getMessage()
                            )));
                });
    }

    @GetMapping("/repositories/{owner}/{repo}/branches")
    @Operation(summary = "Get branches for a specific repository")
    public Mono<ResponseEntity<Object>> getBranches(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String owner,
            @PathVariable String repo) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header")));
        }

        String authToken = authHeader.substring(7); // Remove "Bearer " prefix

        return gitHubService.getRepositoryBranches(authToken, owner, repo)
                .collectList()
                .map(branches -> ResponseEntity.ok().body((Object) branches))
                .onErrorResume(e -> {
                    log.error("Error fetching branches for {}/{}: {}", owner, repo, e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body((Object) Map.of(
                                    "error", "Failed to fetch branches",
                                    "details", e.getMessage()
                            )));
                });
    }
}
