package com.evidencevault.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final int limit;
    private final Map<String, ArrayDeque<Long>> attempts = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(@Value("${app.login.rate-limit-per-minute:10}") int limit) {
        this.limit = limit;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod()) || !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();
        ArrayDeque<Long> queue = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && now - queue.peekFirst() >= 60_000L) queue.removeFirst();
            if (queue.size() >= limit) {
                response.setStatus(429);
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.getWriter().write("<h1>Too many login attempts</h1><p>Please wait a minute and try again.</p>");
                return;
            }
            queue.addLast(now);
            if (queue.size() > limit * 2) {
                while (queue.size() > limit) queue.removeFirst();
            }
        }
        chain.doFilter(request, response);
    }
}
