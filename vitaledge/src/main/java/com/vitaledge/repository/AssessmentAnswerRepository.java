package com.vitaledge.repository;

import com.vitaledge.domain.assessment.AssessmentAnswer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, UUID> {

    List<AssessmentAnswer> findByAssessmentId(UUID assessmentId);

    Optional<AssessmentAnswer> findByAssessmentIdAndQuestionId(UUID assessmentId, UUID questionId);

    @Query("SELECT a FROM AssessmentAnswer a JOIN a.question q "
            + "WHERE a.assessment.id = :assessmentId AND q.key IN :keys")
    List<AssessmentAnswer> findByAssessmentIdAndQuestionKeyIn(@Param("assessmentId") UUID assessmentId,
                                                             @Param("keys") List<String> keys);

    long countByAssessmentId(UUID assessmentId);

    @Modifying
    @Query("DELETE FROM AssessmentAnswer a WHERE a.assessment.id = :assessmentId")
    void deleteByAssessmentId(@Param("assessmentId") UUID assessmentId);
}