package com.apitest.apitest.github.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {
    private Long id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("private")
    private Boolean isPrivate;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String description;
    @JsonProperty("fork")
    private Boolean isFork;
    private String url;
    @JsonProperty("branches_url")
    private String branchesUrl;
}
