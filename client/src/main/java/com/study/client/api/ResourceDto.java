package com.study.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDto {
    public Long resourceId;
    public String title;
    public String type;
    public String pathOrUrl;
}
