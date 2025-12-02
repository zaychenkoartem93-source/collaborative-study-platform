package com.study.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String details;

    public ActivityLog() {}

    public ActivityLog(User user, String action, String details) {
        this.user = user;
        this.action = action;
        this.details = details;
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
