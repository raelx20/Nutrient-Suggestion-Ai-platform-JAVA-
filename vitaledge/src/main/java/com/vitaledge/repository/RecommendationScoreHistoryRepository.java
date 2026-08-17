package com.vitaledge.repository;

import com.vitaledge.domain.recommendation.RecommendationScoreHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationScoreHistoryRepository extends JpaRepository<RecommendationScoreHistory, UUID> {

    List<RecommendationScoreHistory> findByRecommendationId(UUID recommendationId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            "DELETE FROM RecommendationScoreHistory h WHERE h.recommendation.id IN "
                    + "(SELECT r.id FROM Recommendation r WHERE r.assessment.id = :assessmentId)")
    void deleteByAssessmentId(@org.springframework.data.repository.query.Param("assessmentId") UUID assessmentId);
}