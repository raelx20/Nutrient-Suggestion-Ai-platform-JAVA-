package com.vitaledge.web.controller;

import com.vitaledge.common.exception.UnauthorizedException;
import com.vitaledge.security.SecurityUtils;
import com.vitaledge.service.AuthService;
import com.vitaledge.web.dto.auth.LoginRequest;
import com.vitaledge.web.dto.auth.RefreshTokenRequest;
import com.vitaledge.web.dto.auth.RegisterRequest;
import com.vitaledge.web.dto.auth.TokenResponse;
import com.vitaledge.web.dto.auth.UserResponse;
import com.vitaledge.web.dto.auth.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request,
                                                        HttpServletRequest http) {
        AuthService.RegisterResult result = authService.register(request, clientIp(http));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "user", UserResponse.from(result.user()),
                "verification_token", result.verificationToken()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(request, clientIp(http)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody(required = false) RefreshTokenRequest request,
                                                      HttpServletRequest http) {
        UUID userId = SecurityUtils.currentUserId();
        String accessToken = bearerToken(http);
        String refreshToken = request == null ? null : request.refreshToken();
        authService.logout(accessToken, refreshToken, userId);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.me(SecurityUtils.currentUserId()));
    }

    private String bearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Not authenticated.");
        }
        return header.substring(7).trim();
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}