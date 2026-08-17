package com.vitaledge.repository;

import com.vitaledge.domain.assessment.HealthProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, UUID> {

    Optional<HealthProfile> findByIdAndUserId(UUID id, UUID userId);

    Optional<HealthProfile> findByAssessmentIdAndUserId(UUID assessmentId, UUID userId);

    Optional<HealthProfile> findFirstByUserIdOrderByVersionDesc(UUID userId);

    List<HealthProfile> findByUserIdOrderByCreatedAtDesc(UUID userId);
}