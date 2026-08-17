package com.vitaledge.repository;

import com.vitaledge.domain.assessment.Questionnaire;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, UUID> {

    Optional<Questionnaire> findByCode(String code);

    Optional<Questionnaire> findByDefaultQuestionnaireTrueAndActiveTrue();
}