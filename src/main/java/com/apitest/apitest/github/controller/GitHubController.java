package com.apitest.apitest.github.controller;

import com.apitest.apitest.github.model.dto.GitHubWatchRequest;
import com.apitest.apitest.github.model.dto.GitHubRepository;
import com.apitest.apitest.github.model.dto.GitHubBranch;
import com.apitest.apitest.github.model.entity.GitHubFileWatch;
import com.apitest.apitest.github.repository.GitHubFileWatchRepository;
import com.apitest.apitest.github.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Tag(name = "GitHub", description = "GitHub Integration APIs")
@SecurityRequirement(name = "bearerAuth")
public class GitHubController {

    private final GitHubService gitHubService;
    private final ObjectMapper objectMapper;
    private final GitHubFileWatchRepository fileWatchRepository;

    @Value("${github.webhook.secret:}")
    private String webhookSecret;

    @GetMapping("/login")
    @Operation(summary = "Redirect to GitHub for OAuth2 login copy url and paste in browser")
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

    @PostMapping("/webhooks")
    @Operation(summary = "Create a GitHub repository webhook for push and pull_request events")
    public Mono<ResponseEntity<Object>> createWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header")));
        }

        String token = authHeader.substring(7);
        String owner = body.get("owner");
        String repo = body.get("repo");
        String callbackUrl = body.get("callbackUrl");
        String secret = body.getOrDefault("secret", UUID.randomUUID().toString());

        if (owner == null || owner.isBlank() || repo == null || repo.isBlank() || callbackUrl == null || callbackUrl.isBlank()) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Required fields: owner, repo, callbackUrl")));
        }

        return gitHubService.createWebhook(token, owner, repo, callbackUrl, secret)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body((Object) Map.of(
                                "message", "Webhook created",
                                "owner", owner,
                                "repo", repo,
                                "callbackUrl", callbackUrl,
                                "secret", secret
                        )))
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body((Object) Map.of(
                                "error", "Failed to create webhook",
                                "details", e.getMessage()
                        ))));
    }

    @PostMapping("/polling/watch")
    @Operation(summary = "Create a polling-based watch for a file path in a repo")
    public Mono<ResponseEntity<Object>> createPollingWatch(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody GitHubWatchRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header")));
        }

        String token = authHeader.substring(7);
        String branchRef = request.getBranch().startsWith("refs/") ? request.getBranch() : ("refs/heads/" + request.getBranch());

        GitHubFileWatch watch = GitHubFileWatch.builder()
                .userId(request.getUserId())
                .owner(request.getOwner())
                .repo(request.getRepo())
                .branch(branchRef)
                .path(request.getPath())
                .oauthToken(token)
                .build();

        return fileWatchRepository.save(watch)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body((Object) saved))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Failed to save watch", "details", e.getMessage()))));
    }

    @PostMapping("/webhook")
    @Operation(summary = "GitHub webhook receiver (push events)")
    public Mono<ResponseEntity<Object>> receiveWebhook(
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(name = "X-GitHub-Event", required = false) String event,
            @RequestBody Mono<String> body) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret is not configured; rejecting webhook");
            return Mono.just(ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("error", "Webhook secret not configured")));
        }

        if (signature == null || !signature.startsWith("sha256=")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid signature")));
        }

        return body.flatMap(payload -> {
            if (!isValidSignature(payload, signature.substring(7), webhookSecret)) {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Signature verification failed")));
            }

            if (event == null || !event.equals("push")) {
                return Mono.just(ResponseEntity.ok(Map.of("message", "Event ignored")));
            }

            try {
                JsonNode root = objectMapper.readTree(payload);
                String ref = root.path("ref").asText("");
                String repo = root.path("repository").path("name").asText("");
                String owner = root.path("repository").path("owner").path("login").asText("");

                if (ref.isEmpty() || repo.isEmpty() || owner.isEmpty()) {
                    return Mono.just(ResponseEntity.ok(Map.of("message", "Missing key fields")));
                }

                for (JsonNode commit : root.path("commits")) {
                    String sha = commit.path("id").asText("");
                    String authorLogin = commit.path("author").path("username").asText(commit.path("author").path("name").asText(""));
                    String authorEmail = commit.path("author").path("email").asText("");
                    JsonNode added = commit.path("added");
                    JsonNode modified = commit.path("modified");

                    log.info("push on {} {}:{} sha={} by {} <{}> added={} modified={}", ref, owner, repo, sha, authorLogin, authorEmail, added, modified);
                }

                return Mono.just(ResponseEntity.ok(Map.of(
                        "message", "Processed",
                        "repository", owner + "/" + repo,
                        "ref", ref
                )));
            } catch (Exception e) {
                log.error("Webhook processing failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid payload", "details", e.getMessage())));
            }
        });
    }

    private static boolean isValidSignature(String payload, String signatureHex, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String expectedHex = bytesToHex(expected);
            return constantTimeEquals(expectedHex, signatureHex);
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] HEX = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
