package com.study.platform.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private final SimpMessagingTemplate template;

    public NotificationService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void sendNewTask(Long groupId, Long taskId, String title) {
        template.convertAndSend("/topic/group/" + groupId,
                Map.of("type", "NEW_TASK", "taskId", taskId, "title", title));
    }

    public void sendTaskUpdated(Long groupId, Long taskId, String status) {
        template.convertAndSend("/topic/group/" + groupId,
                Map.of("type", "TASK_UPDATED", "taskId", taskId, "status", status));
    }

    public void sendResourceAdded(Long groupId, Long resourceId, String title) {
        template.convertAndSend("/topic/group/" + groupId,
                Map.of("type", "NEW_FILE", "resourceId", resourceId, "title", title));
    }

    public void sendNewMember(Long groupId, Long userId, String userName) {
        template.convertAndSend("/topic/group/" + groupId,
                Map.of("type", "NEW_MEMBER", "userId", userId, "userName", userName));
    }
}
