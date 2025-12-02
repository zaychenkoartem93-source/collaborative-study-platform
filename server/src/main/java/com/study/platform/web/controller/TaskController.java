package com.study.platform.web.controller;

import com.study.platform.domain.Group;
import com.study.platform.domain.Task;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.UserRepository;
import com.study.platform.service.GroupService;
import com.study.platform.service.TaskService;
import com.study.platform.web.SessionUserResolver;
import com.study.platform.web.dto.CreateTaskRequest;
import com.study.platform.web.dto.UpdateTaskRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService tasks;
    private final GroupRepository groups;
    private final GroupService groupService;
    private final UserRepository users;
    private final SessionUserResolver userResolver;

    public TaskController(TaskService tasks,
                          GroupRepository groups,
                          GroupService groupService,
                          UserRepository users,
                          SessionUserResolver userResolver) {
        this.tasks = tasks;
        this.groups = groups;
        this.groupService = groupService;
        this.users = users;
        this.userResolver = userResolver;
    }

    private User currentUser(HttpServletRequest request) {
        return userResolver.requireUser(request);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateTaskRequest req, HttpServletRequest request) {
        User u = currentUser(request);
        Group g = groups.findById(req.getGroupId()).orElse(null);
        if (g == null) return ResponseEntity.status(404).body(Map.of("error", "Group not found"));
        if (!groupService.isMember(u, g)) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        Task t = tasks.createTask(req.getGroupId(), u, req.getTitle(), req.getDescription(), req.getStatus(), req.getDeadline());
        return ResponseEntity.ok(Map.of("taskId", t.getTaskId(), "title", t.getTitle()));
    }

    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<?> byGroup(@PathVariable("groupId") Long groupId, HttpServletRequest request) {
        User u = currentUser(request);
        Group g = groups.findById(groupId).orElse(null);
        if (g == null) return ResponseEntity.status(404).body(Map.of("error", "Group not found"));
        if (!groupService.isMember(u, g)) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        return ResponseEntity.ok(tasks.listByGroup(groupId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> update(@PathVariable("taskId") Long taskId, @RequestBody UpdateTaskRequest req, HttpServletRequest request) {
        User u = currentUser(request);
        Task t = tasks.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!groupService.isMember(u, t.getGroup())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        Task updated = tasks.updateTask(taskId, req.getTitle(), req.getDescription(), req.getStatus(), req.getDeadline());
        return ResponseEntity.ok(Map.of("taskId", updated.getTaskId(), "status", updated.getStatus().name()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> delete(@PathVariable("taskId") Long taskId, HttpServletRequest request) {
        User u = currentUser(request);
        Task t = tasks.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!groupService.isMember(u, t.getGroup())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        tasks.deleteTask(taskId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable("taskId") Long taskId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        User u = currentUser(request);
        Task t = tasks.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!groupService.isMember(u, t.getGroup())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        String newStatus = body.get("status");
        if (newStatus == null) return ResponseEntity.badRequest().body(Map.of("error", "Missing status"));
        Task.Status status;
        try {
            status = Task.Status.valueOf(newStatus);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
        t = tasks.updateTask(taskId, t.getTitle(), t.getDescription(), status, t.getDeadline());
        return ResponseEntity.ok(Map.of("taskId", t.getTaskId(), "status", t.getStatus().name()));
    }
}
