package com.study.client.api;

import java.util.Map;

public class GroupStatsResponse {

    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByAuthor;
    private long resourcesCount;

    public Map<String, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public Map<String, Long> getTasksByAuthor() {
        return tasksByAuthor;
    }

    public long getResourcesCount() {
        return resourcesCount;
    }
}
