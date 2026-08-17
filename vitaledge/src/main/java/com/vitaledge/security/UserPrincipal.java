package com.vitaledge.security;

import com.vitaledge.domain.user.Role;
import com.vitaledge.domain.user.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Spring Security principal wrapping the authenticated {@link User}.
 */
@SuppressWarnings("null")
public record UserPrincipal(
        UUID userId,
        String email,
        String fullName,
        boolean active,
        boolean verified,
        Collection<? extends GrantedAuthority> authorities
) {

    public static UserPrincipal from(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .filter(java.util.Objects::nonNull)
                .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
                .map(a -> (GrantedAuthority) a)
                .toList();
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.isVerified(),
                authorities);
    }

    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    public List<String> roleNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .toList();
    }
}