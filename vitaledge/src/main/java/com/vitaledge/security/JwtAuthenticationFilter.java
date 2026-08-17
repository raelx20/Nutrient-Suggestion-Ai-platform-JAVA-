package com.vitaledge.security;

import com.vitaledge.repository.UserRepository;
import com.vitaledge.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the {@code Authorization: Bearer <access token>} header on every request.
 * Explicitly rejects blacklisted (logged-out) tokens and wrong-type tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService blacklistService,
                                   UserRepository userRepository) {
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX) && !header.isBlank()) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            String tokenHash = JwtService.hashToken(token);
            if (!blacklistService.isBlacklisted(tokenHash)) {
                Map<String, Object> claims = jwtService.decodeAccessToken(token);
                if (claims != null && claims.get("sub") != null) {
                    authenticate(request, claims.get("sub").toString());
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String subject) {
        try {
            UUID userId = UUID.fromString(subject);
            userRepository.findById(userId).ifPresent(user -> {
                if (user.isActive()) {
                    UserPrincipal principal = UserPrincipal.from(user);
                    var authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.authorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            });
        } catch (IllegalArgumentException e) {
            // Malformed subject: leave unauthenticated
        }
    }
}