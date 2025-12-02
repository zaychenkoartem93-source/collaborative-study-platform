package com.study.platform.web.controller;

import com.study.platform.domain.Group;
import com.study.platform.domain.Task;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.ResourceRepository;
import com.study.platform.repository.TaskRepository;
import com.study.platform.repository.UserRepository;
import com.study.platform.service.GroupService;
import com.study.platform.web.SessionUserResolver;
import com.study.platform.web.dto.GroupStatsResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final TaskRepository tasks;
    private final ResourceRepository resources;
    private final GroupRepository groups;
    private final GroupService groupService;
    private final UserRepository users;
    private final SessionUserResolver userResolver;

    public StatsController(TaskRepository tasks,
                           ResourceRepository resources,
                           GroupRepository groups,
                           GroupService groupService,
                           UserRepository users,
                           SessionUserResolver userResolver) {
        this.tasks = tasks;
        this.resources = resources;
        this.groups = groups;
        this.groupService = groupService;
        this.users = users;
        this.userResolver = userResolver;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> groupStats(@PathVariable("groupId") Long groupId, HttpServletRequest request) {
        User u = userResolver.requireUser(request);
        Group g = groups.findById(groupId).orElseThrow();
        if (!groupService.isMember(u, g)) return ResponseEntity.status(403).build();

        Map<String, Long> tasksByStatus = new HashMap<>();
        for (Task.Status s : Task.Status.values()) {
            long count = tasks.findByGroupAndStatus(g, s).size();
            tasksByStatus.put(s.name(), count);
        }

        Map<String, Long> tasksByAuthor = new HashMap<>();
        tasks.findByGroup(g).forEach(t ->
                tasksByAuthor.merge(t.getCreatedBy().getEmail(), 1L, Long::sum)
        );

        long resourcesCount = resources.findByGroup(g).size();

        return ResponseEntity.ok(new GroupStatsResponse(tasksByStatus, tasksByAuthor, resourcesCount));
    }
}