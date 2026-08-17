package com.vitaledge.repository;

import com.vitaledge.domain.user.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserId(UUID userId);

    List<RefreshToken> findByFamilyId(UUID familyId);

    long countByUserIdAndRevokedAtIsNullAndExpiresAtAfter(UUID userId, LocalDateTime now);

    long deleteByUserIdAndRevokedAtIsNotNull(UUID userId);

    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}