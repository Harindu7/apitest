package com.apitest.apitest.github.service;

import com.apitest.apitest.github.model.dto.GitHubAuthResponse;
import com.apitest.apitest.github.model.dto.GitHubBranch;
import com.apitest.apitest.github.model.dto.GitHubRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GitHubService {

    Mono<GitHubAuthResponse> getAccessToken(String code);

    Mono<String> getGitHubAuthorizeUrl();

    Flux<GitHubRepository> getUserRepositories(String token);

    Flux<GitHubBranch> getRepositoryBranches(String token, String owner, String repo);

    /**
     * Create a repository webhook that posts events to the provided callback URL.
     * The caller's token must have admin access to the repository.
     *
     * @param token OAuth token of the caller (without the Bearer prefix)
     * @param owner Repository owner
     * @param repo Repository name
     * @param callbackUrl Public URL to receive webhook events
     * @param secret Shared secret to validate webhook signatures
     * @return a Mono completing when the webhook is created
     */
    Mono<Void> createWebhook(String token, String owner, String repo, String callbackUrl, String secret);

    /**
     * Get the latest commit (if any) that touched the given path on the branch.
     */
    Mono<String> getLatestCommitShaForPath(String token, String owner, String repo, String branch, String path);

    /**
     * Get added lines for a file in a specific commit. Returns empty list if the file wasn't part of the commit or no additions.
     */
    Flux<String> getAddedLinesInCommitForPath(String token, String owner, String repo, String commitSha, String path);
}
