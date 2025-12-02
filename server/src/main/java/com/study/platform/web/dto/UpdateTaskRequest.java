package com.study.platform.web.dto;

import com.study.platform.domain.Task;

import java.time.Instant;

public class UpdateTaskRequest {
    private String title;
    private String description;
    private Task.Status status;
    private Instant deadline;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Task.Status getStatus() { return status; }
    public void setStatus(Task.Status status) { this.status = status; }
    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }
}
