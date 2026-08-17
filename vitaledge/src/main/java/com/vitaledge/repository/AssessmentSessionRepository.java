package com.vitaledge.repository;

import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.AssessmentStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentSessionRepository extends JpaRepository<AssessmentSession, UUID> {

    Optional<AssessmentSession> findByIdAndUserId(UUID id, UUID userId);

    Optional<AssessmentSession> findByIdAndUserIdAndStatus(UUID id, UUID userId, AssessmentStatus status);

    Optional<AssessmentSession> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime since);

    @Modifying
    @Query("UPDATE AssessmentSession a SET a.status = :abandoned, a.abandonedAt = :now "
            + "WHERE a.user.id = :userId AND a.status = :inProgress AND a.completedAt IS NULL")
    int abandonIncompleteForUser(@Param("userId") UUID userId,
                                 @Param("abandoned") AssessmentStatus abandoned,
                                 @Param("inProgress") AssessmentStatus inProgress,
                                 @Param("now") LocalDateTime now);
}