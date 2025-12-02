package com.study.platform.web.controller;

import com.study.platform.domain.User;
import com.study.platform.repository.UserRepository;
import com.study.platform.service.UserService;
import com.study.platform.web.SessionUserResolver;
import com.study.platform.web.dto.UserProfileDto;
import com.study.platform.web.dto.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService users;
    private final UserRepository userRepo;
    private final SessionUserResolver userResolver;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService users,
                          UserRepository userRepo,
                          SessionUserResolver userResolver,
                          PasswordEncoder passwordEncoder) {
        this.users = users;
        this.userRepo = userRepo;
        this.userResolver = userResolver;
        this.passwordEncoder = passwordEncoder;
    }

    private User currentUser(HttpServletRequest request) {
        return userResolver.requireUser(request);
    }

    // Получение профиля
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        User u = currentUser(request);
        UserProfileDto profile = new UserProfileDto(
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getPhone()  // === Добавлено поле телефона ===
        );
        return ResponseEntity.ok(profile);
    }

    // Обновление профиля
    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateRequest req, HttpServletRequest request) {
        User u = currentUser(request);

        try {
            // Обновляем через сервис
            User updated = users.updateProfile(
                    u.getUserId(),
                    req.getName(),
                    req.getEmail(),
                    req.getPhone()  // === Добавлено поле телефона ===
            );

            return ResponseEntity.ok(Map.of(
                    "userId", updated.getUserId(),
                    "name", updated.getName(),
                    "email", updated.getEmail(),
                    "phone", updated.getPhone(),  // === Добавлено поле телефона ===
                    "message", "Profile updated successfully"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Изменение пароля
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User u = currentUser(request);

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid passwords"));
        }

        // Проверяем старый пароль
        if (!passwordEncoder.matches(oldPassword, u.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Old password incorrect"));
        }

        try {
            // Обновляем пароль через сервис
            User updated = users.updatePassword(u.getUserId(), newPassword);
            userRepo.save(updated);

            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
