package com.diago.config;

import com.diago.model.User;
import com.diago.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository users;

    public CurrentUser(UserRepository users) { this.users = users; }

    /** Utilisateur connecté, ou null. */
    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return users.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }
}
