package com.study.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resources")
public class ResourceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type;

    @Column(name = "path_or_url", nullable = false)
    private String pathOrUrl;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    public ResourceItem() {}

    public ResourceItem(Group group, User uploadedBy, String title, String type, String pathOrUrl) {
        this.group = group;
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.type = type;
        this.pathOrUrl = pathOrUrl;
    }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPathOrUrl() { return pathOrUrl; }
    public void setPathOrUrl(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
