package com.evidencevault.security;

import com.evidencevault.entity.User;
import com.evidencevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginSecurityService {
    private final UserRepository users;
    private final int maxFailures;
    private final int lockMinutes;

    public LoginSecurityService(UserRepository users,
                                @Value("${app.login.max-failures:5}") int maxFailures,
                                @Value("${app.login.lock-minutes:15}") int lockMinutes) {
        this.users = users;
        this.maxFailures = maxFailures;
        this.lockMinutes = lockMinutes;
    }

    @Transactional
    public void recordFailure(String username) {
        if (username == null || username.isBlank()) return;
        users.findByUsername(username).ifPresent(user -> {
            if (user.isLocked() && user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                return;
            }
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailures) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
                user.setFailedLoginAttempts(0);
            }
            users.save(user);
        });
    }

    @Transactional
    public void recordSuccess(String username) {
        users.findByUsername(username).ifPresent(user -> {
            if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                users.save(user);
            }
        });
    }

    public boolean isLocked(User user) {
        if (user.getLockedUntil() == null) return false;
        if (user.getLockedUntil().isAfter(LocalDateTime.now())) return true;
        return false;
    }
}
