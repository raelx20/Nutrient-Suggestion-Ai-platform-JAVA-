package com.vitaledge.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns a correlation {@code X-Request-ID} to every request for traceability.
 */
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_NAME);
        if (requestId == null || requestId.isBlank() || requestId.length() > 64) {
            requestId = UUID.randomUUID().toString();
        }
        request.setAttribute(HEADER_NAME, requestId);
        response.setHeader(HEADER_NAME, requestId);
        filterChain.doFilter(request, response);
    }
}