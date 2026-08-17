package com.vitaledge.repository;

import com.vitaledge.domain.assessment.Question;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    Optional<Question> findByIdAndQuestionnaireId(UUID id, UUID questionnaireId);

    List<Question> findByQuestionnaireIdAndActiveTrueOrderByOrderIndexAsc(UUID questionnaireId);
}