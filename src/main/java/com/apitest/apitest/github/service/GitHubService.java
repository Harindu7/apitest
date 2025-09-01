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
}
