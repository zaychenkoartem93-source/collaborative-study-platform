package com.study.platform.web.controller;

import com.study.platform.domain.Group;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.UserRepository;
import com.study.platform.service.GroupService;
import com.study.platform.web.SessionUserResolver;
import com.study.platform.web.dto.CreateGroupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groups;
    private final UserRepository users;
    private final GroupRepository groupRepo;
    private final SessionUserResolver userResolver;

    public GroupController(GroupService groups,
                           UserRepository users,
                           GroupRepository groupRepo,
                           SessionUserResolver userResolver) {
        this.groups = groups;
        this.users = users;
        this.groupRepo = groupRepo;
        this.userResolver = userResolver;
    }

    private User currentUser(HttpServletRequest request) {
        return userResolver.requireUser(request);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateGroupRequest req, HttpServletRequest request) {
        User u = currentUser(request);
        Group g = groups.createGroup(u, req.getName(), req.getDescription());
        return ResponseEntity.ok(Map.of("groupId", g.getGroupId(), "name", g.getName()));
    }

    @GetMapping("/my")
    public ResponseEntity<?> my(HttpServletRequest request) {
        User u = currentUser(request);
        return ResponseEntity.ok(groups.myGroups(u));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id, HttpServletRequest request) {
        User u = currentUser(request);
        Group g = groupRepo.findById(id).orElseThrow();
        if (!groups.isMember(u, g)) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        return ResponseEntity.ok(g);
    }

    // Новый endpoint для добавления участника по email
    @PostMapping("/{id}/add-member")
    public ResponseEntity<?> addMember(
            @PathVariable("id") Long groupId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        User requester = currentUser(request);
        Group group = groupRepo.findById(groupId).orElse(null);
        if (group == null) return ResponseEntity.status(404).body(Map.of("error", "Group not found"));
        if (!groups.isMember(requester, group)) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing email"));
        }
        User newMember = users.findByEmail(email).orElse(null);
        if (newMember == null)
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        boolean added = groups.addMember(group, newMember); // реализация в GroupService

        if (added) {
            return ResponseEntity.ok(Map.of("message", "User added"));
        } else {
            return ResponseEntity.status(409).body(Map.of("error", "User is already a member"));
        }
    }
}
