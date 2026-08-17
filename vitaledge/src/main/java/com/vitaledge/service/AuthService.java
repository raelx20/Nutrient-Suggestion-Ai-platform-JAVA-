package com.vitaledge.service;

import com.vitaledge.common.exception.ApiException;
import com.vitaledge.common.exception.ForbiddenException;
import com.vitaledge.common.exception.RateLimitException;
import com.vitaledge.common.exception.UnauthorizedException;
import com.vitaledge.common.util.EmailNormalizer;
import com.vitaledge.config.ApplicationProperties;
import com.vitaledge.domain.user.RefreshToken;
import com.vitaledge.domain.user.Role;
import com.vitaledge.domain.user.User;
import com.vitaledge.repository.RefreshTokenRepository;
import com.vitaledge.repository.RoleRepository;
import com.vitaledge.repository.UserRepository;
import com.vitaledge.security.JwtService;
import com.vitaledge.web.dto.auth.LoginRequest;
import com.vitaledge.web.dto.auth.RegisterRequest;
import com.vitaledge.web.dto.auth.TokenResponse;
import com.vitaledge.web.dto.auth.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication and registration. Ports the source project's {@code auth_service.py}
 * and the {@code /api/v1/auth} router: email normalization, BCrypt hashing, JWT
 * access + rotating refresh tokens, rate limiting, email verification and logout blacklist.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String ROLE_CONSUMER = "CONSUMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final RateLimitService rateLimitService;
    private final AuditLogService auditLogService;
    private final ApplicationProperties properties;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, TokenBlacklistService blacklistService,
                       RateLimitService rateLimitService, AuditLogService auditLogService,
                       ApplicationProperties properties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.rateLimitService = rateLimitService;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Transactional
    public RegisterResult register(RegisterRequest request, String ip) {
        String email = EmailNormalizer.normalize(request.email());

        int limit = properties.getRateLimit().getRegistrationPerIpPerHour();
        rateLimitService.check("register", ip, limit, 3600);

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.");
        }

        Role consumerRole = roleRepository.findByName(ROLE_CONSUMER)
                .orElseThrow(() -> new IllegalStateException("CONSUMER role is not seeded"));

        User user = new User(email, passwordEncoder.encode(request.password()), request.fullName());
        user.addRole(consumerRole);
        userRepository.save(user);

        String verificationToken = jwtService.createEmailVerificationToken(user.getId().toString());

        auditLogService.record("auth.register", "user", user.getId(),
                Map.of("email", email), ip, "Registration", Optional.of(user));

        return new RegisterResult(user, verificationToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        Map<String, Object> claims = jwtService.decodeEmailVerificationToken(token);
        if (claims == null || claims.get("sub") == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_TOKEN",
                    "The email verification token is invalid or has expired.");
        }
        UUID userId;
        try {
            userId = UUID.fromString(claims.get("sub").toString());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_TOKEN",
                    "The email verification token is invalid.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "User not found."));
        user.markVerified();
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ip) {
        String email = EmailNormalizer.normalize(request.email());

        rateLimitService.check("login:ip", ip, properties.getRateLimit().getLoginPerIpPerHour(), 3600);
        rateLimitService.check("login:email", email, properties.getRateLimit().getLoginPerEmailPerHour(), 3600);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (user.isLocked()) {
            throw new ForbiddenException("Account is locked. Please contact support.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        if (!user.isVerified()) {
            throw new UnauthorizedException("Email not verified. Please verify your email first.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);

        auditLogService.record("auth.login", "user", user.getId(),
                Map.of("email", email), ip, "Login", Optional.of(user));

        return issueTokenPair(user);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        Map<String, Object> claims = jwtService.decodeRefreshToken(rawRefreshToken);
        if (claims == null || claims.get("sub") == null) {
            throw new UnauthorizedException("Invalid or expired refresh token.");
        }
        String tokenHash = JwtService.hashToken(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token has been revoked."));

        if (!stored.isActive()) {
            throw new UnauthorizedException("Refresh token has been revoked or expired.");
        }
        UUID userId = stored.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
        if (!user.isActive() || user.isLocked()) {
            throw new ForbiddenException("Account is not active.");
        }

        return rotateRefreshToken(stored, user);
    }

    private TokenResponse rotateRefreshToken(RefreshToken oldToken, User user) {
        String newRefreshJwt = jwtService.createRefreshToken(user.getId().toString());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusDays(properties.getSecurity().getRefreshTokenExpireDays());
        RefreshToken newToken = new RefreshToken(user, JwtService.hashToken(newRefreshJwt),
                oldToken.getFamilyId(), expiresAt);
        refreshTokenRepository.save(newToken);

        oldToken.revokeAndReplace(newToken.getId());
        refreshTokenRepository.save(oldToken);

        return buildTokenResponse(user, newRefreshJwt);
    }

    @Transactional
    public void logout(String rawAccessToken, String rawRefreshToken, UUID userId) {
        if (rawAccessToken != null && !rawAccessToken.isBlank()) {
            String hash = JwtService.hashToken(rawAccessToken);
            long ttl = accessTokenRemainingSeconds(rawAccessToken);
            if (ttl > 0) {
                blacklistService.blacklist(hash, ttl);
            }
        }
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(JwtService.hashToken(rawRefreshToken))
                    .ifPresent(stored -> {
                        refreshTokenRepository.findByFamilyId(stored.getFamilyId())
                                .forEach(t -> {
                                    t.revoke();
                                    refreshTokenRepository.save(t);
                                });
                    });
        }
        auditLogService.record("auth.logout", "user", userId, Map.of(), null, null,
                userRepository.findById(userId));
    }

    private long accessTokenRemainingSeconds(String token) {
        Map<String, Object> claims = jwtService.decodeAccessToken(token);
        if (claims == null || claims.get("exp") == null) {
            return 0;
        }
        long exp = Long.parseLong(claims.get("exp").toString());
        return Math.max(0, exp - (System.currentTimeMillis() / 1000));
    }

    private TokenResponse issueTokenPair(User user) {
        String refreshToken = jwtService.createRefreshToken(user.getId().toString());
        return issueTokens(user, refreshToken);
    }

    private TokenResponse issueTokens(User user, String refreshToken) {
        UUID familyId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusDays(properties.getSecurity().getRefreshTokenExpireDays());
        refreshTokenRepository.save(new RefreshToken(user, JwtService.hashToken(refreshToken), familyId, expiresAt));
        return buildTokenResponse(user, refreshToken);
    }

    private TokenResponse buildTokenResponse(User user, String refreshJwt) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtService.createAccessToken(user.getId().toString(), roles);
        return new TokenResponse(
                accessToken,
                refreshJwt,
                "Bearer",
                properties.getSecurity().getAccessTokenExpireMinutes() * 60,
                UserResponse.from(user));
    }

    public UserResponse me(UUID userId) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
        return UserResponse.from(user);
    }

    public record RegisterResult(User user, String verificationToken) {
    }
}