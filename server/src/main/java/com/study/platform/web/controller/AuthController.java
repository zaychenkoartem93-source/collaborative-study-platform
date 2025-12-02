package com.study.platform.web.controller;

import com.study.platform.domain.User;
import com.study.platform.service.UserService;
import com.study.platform.web.dto.LoginRequest;
import com.study.platform.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Collections; // не забудь импорт сверху файла

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final PasswordEncoder encoder;

    public AuthController(UserService users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // ----------------------------------------------------------
    // REGISTER
    // ----------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User u = users.register(req.getName(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok(Map.of(
                "userId", u.getUserId(),
                "email", u.getEmail(),
                "name", u.getName()
        ));
    }

    // ----------------------------------------------------------
    // LOGIN  (JSON login – интеграция с Spring Security)
    // ----------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {

        Optional<User> opt = users.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User u = opt.get();

        if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // ✔ Создаём Authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                u,
                null,
                Collections.emptyList()
        );

        // ✔ Записываем в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);

        // ✔ Создаём сессию и кладём userId
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", u.getUserId());

        return ResponseEntity.ok(Map.of(
                "message", "ok",
                "userId", u.getUserId(),
                "name", u.getName(),
                "email", u.getEmail()
        ));
    }


    // ----------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null)
            session.invalidate();

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message", "logged out"));
    }

    // ----------------------------------------------------------
    // SESSION CHECK
    // ----------------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
