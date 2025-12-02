package com.study.platform.web;

import com.study.platform.domain.User;
import com.study.platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SessionUserResolver {

    private final UserRepository users;

    public SessionUserResolver(UserRepository users) {
        this.users = users;
    }

    public User requireUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No session");
        }
        Object val = session.getAttribute("userId");
        if (!(val instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No user");
        }
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}