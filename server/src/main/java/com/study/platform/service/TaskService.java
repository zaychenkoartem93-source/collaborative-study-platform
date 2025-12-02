package com.study.platform.service;

import com.study.platform.domain.Group;
import com.study.platform.domain.Task;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.TaskRepository;
import com.study.platform.websocket.NotificationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final GroupRepository groups;
    private final NotificationService notifier;

    public TaskService(TaskRepository tasks,
                       GroupRepository groups,
                       NotificationService notifier) {
        this.tasks = tasks;
        this.groups = groups;
        this.notifier = notifier;
    }

    public Task createTask(Long groupId,
                           User author,
                           String title,
                           String description,
                           Task.Status status,
                           Instant deadline) {
        Group g = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        Task t = new Task(
                g,
                author,
                title,
                description,
                status == null ? Task.Status.OPEN : status,
                deadline
        );
        t = tasks.save(t);
        notifier.sendNewTask(g.getGroupId(), t.getTaskId(), t.getTitle());

        return t;
    }

    public List<Task> listByGroup(Long groupId) {
        Group g = groups.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return tasks.findByGroupOrderByCreatedAtDesc(g);
    }

    public Task updateTask(Long taskId,
                           String title,
                           String description,
                           Task.Status status,
                           Instant deadline) {
        Task t = tasks.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (title != null) t.setTitle(title);
        if (description != null) t.setDescription(description);
        if (status != null) t.setStatus(status);
        if (deadline != null) t.setDeadline(deadline);

        t = tasks.save(t);

        notifier.sendTaskUpdated(t.getGroup().getGroupId(), t.getTaskId(), t.getStatus().name());

        return t;
    }

    public Optional<Task> findById(Long taskId) {
        return tasks.findById(taskId);
    }

    public void deleteTask(Long taskId) {
        tasks.deleteById(taskId);
    }
}
