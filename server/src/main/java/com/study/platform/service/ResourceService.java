package com.study.platform.service;

import com.study.platform.domain.Group;
import com.study.platform.domain.ResourceItem;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.ResourceRepository;
import com.study.platform.websocket.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resources;
    private final GroupRepository groups;
    private final NotificationService notifier;

    public ResourceService(ResourceRepository resources,
                           GroupRepository groups,
                           NotificationService notifier) {
        this.resources = resources;
        this.groups = groups;
        this.notifier = notifier;
    }

    public ResourceItem createLink(Long groupId, User user, String title, String url) {
        Group g = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        ResourceItem item = new ResourceItem(g, user, title, "LINK", url);
        item = resources.save(item);

        notifier.sendResourceAdded(g.getGroupId(), item.getResourceId(), item.getTitle());

        return item;
    }

    public ResourceItem createFileResource(Long groupId, User user, String title, String path) {
        Group g = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        ResourceItem item = new ResourceItem(g, user, title, "FILE", path);
        item = resources.save(item);

        notifier.sendResourceAdded(g.getGroupId(), item.getResourceId(), item.getTitle());

        return item;
    }

    public ResourceItem get(Long id) {
        return resources.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    public List<ResourceItem> listByGroup(Long groupId) {
        Group g = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return resources.findByGroupOrderByUploadedAtDesc(g);
    }
    public void delete(Long id) {
        resources.deleteById(id);
    }
}
