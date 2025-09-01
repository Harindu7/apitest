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
public class GitHubBranch {
    private String name;
    @JsonProperty("protected")
    private Boolean isProtected;
    private Commit commit;
    @JsonProperty("_links")
    private Links links;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Commit {
        private String sha;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Links {
        private String self;
        private String html;
    }
}
