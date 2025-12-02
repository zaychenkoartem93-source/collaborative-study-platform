package com.study.platform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateResourceRequest {
    @NotNull
    private Long groupId;

    @NotBlank
    private String title;

    @NotBlank
    private String type; // LINK or FILE (на этом этапе поддержим только LINK)

    @NotBlank
    private String pathOrUrl;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPathOrUrl() { return pathOrUrl; }
    public void setPathOrUrl(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }
}
