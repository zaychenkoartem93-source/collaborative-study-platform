package com.study.platform.web.dto;

import com.study.platform.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CreateTaskRequest {
    @NotNull
    private Long groupId;

    @NotBlank @Size(min = 2, max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    private Instant deadline;

    private Task.Status status = Task.Status.OPEN;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }
    public Task.Status getStatus() { return status; }
    public void setStatus(Task.Status status) { this.status = status; }
}
