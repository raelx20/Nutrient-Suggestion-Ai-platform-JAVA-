package com.vitaledge.repository;

import com.vitaledge.domain.recommendation.Recommendation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    Optional<Recommendation> findByIdAndUserId(UUID id, UUID userId);

    List<Recommendation> findByUserIdOrderByRankAsc(UUID userId);

    List<Recommendation> findByAssessmentIdAndUserIdOrderByRankAsc(UUID assessmentId, UUID userId);

    Page<Recommendation> findAllByOrderByRecommendedAtDesc(Pageable pageable);

    @Modifying
    @Query("DELETE FROM Recommendation r WHERE r.assessment.id = :assessmentId")
    void deleteByAssessmentId(@Param("assessmentId") UUID assessmentId);
}