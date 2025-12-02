package com.study.platform.service;

import com.study.platform.domain.User;
import com.study.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User register(String name, String email, String rawPassword) {
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User u = new User(name, email, encoder.encode(rawPassword));
        return users.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email);
    }

    /**
     * Обновление профиля пользователя
     */
    public User updateProfile(Long userId, String name, String email, String phone) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Проверяем уникальность email
        if (!email.equals(user.getEmail())) {
            if (users.existsByEmailAndUserIdNot(email, userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
        }

        // Обновляем поля
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);  // === Добавлено обновление телефона ===

        return users.save(user);
    }

    /**
     * Обновление пароля пользователя
     */
    public User updatePassword(Long userId, String newPassword) {
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(encoder.encode(newPassword));
        return users.save(user);
    }

    public boolean existsByEmailAndUserIdNot(String email, Long userId) {
        return users.existsByEmailAndUserIdNot(email, userId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("USER")
                .build();
    }
}
