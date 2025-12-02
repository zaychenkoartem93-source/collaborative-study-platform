package com.study.platform.web.controller;

import com.study.platform.domain.Group;
import com.study.platform.domain.ResourceItem;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.UserRepository;
import com.study.platform.service.GroupService;
import com.study.platform.service.ResourceService;
import com.study.platform.web.SessionUserResolver;
import com.study.platform.web.dto.CreateResourceRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final GroupRepository groups;
    private final GroupService groupService;
    private final UserRepository users;
    private final SessionUserResolver userResolver;

    private static final Path UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();

    @Autowired
    public ResourceController(ResourceService resourceService,
                              GroupRepository groups,
                              GroupService groupService,
                              UserRepository users,
                              SessionUserResolver userResolver) {

        this.resourceService = resourceService;
        this.groups = groups;
        this.groupService = groupService;
        this.users = users;
        this.userResolver = userResolver;
    }

    private User currentUser(HttpServletRequest request) {
        return userResolver.requireUser(request);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateResourceRequest req,
                                    HttpServletRequest request) {

        User u = currentUser(request);
        Group g = groups.findById(req.getGroupId()).orElseThrow();

        if (!groupService.isMember(u, g))
            return ResponseEntity.status(403).build();

        return ResponseEntity.ok(
                resourceService.createLink(req.getGroupId(), u, req.getTitle(), req.getPathOrUrl())
        );
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceItem> upload(
            @RequestParam("groupId") Long groupId,
            @RequestParam("title") String title,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) throws IOException {

        Files.createDirectories(UPLOAD_DIR);

        String savePath = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dest = UPLOAD_DIR.resolve(savePath);

        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        ResourceItem item = resourceService.createFileResource(groupId, user, title, savePath);

        return ResponseEntity.ok(item);
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> download(
            @PathVariable("id") Long id
    ) throws IOException {

        ResourceItem item = resourceService.get(id);

        Path path = Paths.get("uploads")
                .resolve(item.getPathOrUrl())
                .normalize();

        UrlResource file = new UrlResource(path.toUri());

        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName().toString() + "\""
                )
                .body(file);
    }



    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<?> byGroup(@PathVariable("groupId") Long groupId,
                                     HttpServletRequest request) {

        User u = currentUser(request);
        Group g = groups.findById(groupId).orElseThrow();

        if (!groupService.isMember(u, g))
            return ResponseEntity.status(403).build();

        return ResponseEntity.ok(resourceService.listByGroup(groupId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable("id") Long id,
                                            HttpServletRequest request) throws IOException {

        User u = currentUser(request);                  // Получаем текущего пользователя
        ResourceItem item = resourceService.get(id);    // Получаем ресурс по ID
        Group group = item.getGroup();                  // Получаем группу ресурса

        if (!groupService.isMember(u, group)) {
            return ResponseEntity.status(403).build();
        }


        if (item.getPathOrUrl() != null && !item.getPathOrUrl().startsWith("http")) {
            Path filePath = UPLOAD_DIR.resolve(item.getPathOrUrl()).normalize();
            Files.deleteIfExists(filePath);
        }

        resourceService.delete(id);

        return ResponseEntity.ok().build();
    }
}
