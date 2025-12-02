package com.study.platform.web.dto;

import java.util.Map;

public class GroupStatsResponse {
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByAuthor;
    private Long resourcesCount;

    public GroupStatsResponse() {}

    public GroupStatsResponse(Map<String, Long> tasksByStatus,
                              Map<String, Long> tasksByAuthor,
                              Long resourcesCount) {
        this.tasksByStatus = tasksByStatus;
        this.tasksByAuthor = tasksByAuthor;
        this.resourcesCount = resourcesCount;
    }

    public Map<String, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public void setTasksByStatus(Map<String, Long> tasksByStatus) {
        this.tasksByStatus = tasksByStatus;
    }

    public Map<String, Long> getTasksByAuthor() {
        return tasksByAuthor;
    }

    public void setTasksByAuthor(Map<String, Long> tasksByAuthor) {
        this.tasksByAuthor = tasksByAuthor;
    }

    public Long getResourcesCount() {
        return resourcesCount;
    }

    public void setResourcesCount(Long resourcesCount) {
        this.resourcesCount = resourcesCount;
    }
}