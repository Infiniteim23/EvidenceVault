package com.evidencevault.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Request-ID";
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id=request.getHeader(HEADER);
        if(id==null || id.isBlank() || id.length()>100) id=UUID.randomUUID().toString();
        MDC.put("requestId", id);
        response.setHeader(HEADER,id);
        try { chain.doFilter(request,response); }
        finally { MDC.remove("requestId"); }
    }
}
