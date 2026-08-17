package com.vitaledge.security;

import com.vitaledge.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helpers for reading the current authenticated principal from the security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return principal;
    }

    public static java.util.UUID currentUserId() {
        return currentPrincipal().userId();
    }
}