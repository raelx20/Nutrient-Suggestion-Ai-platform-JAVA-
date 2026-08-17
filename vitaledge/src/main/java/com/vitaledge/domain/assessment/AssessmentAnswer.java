package com.vitaledge.domain.assessment;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "assessment_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "question_id"}),
        indexes = @Index(name = "idx_answer_assessment", columnList = "assessment_id"))
@Getter
@Setter
public class AssessmentAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AssessmentSession assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "answer_value", nullable = false)
    private Object answerValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_source", nullable = false, length = 20)
    private AnswerSource answerSource = AnswerSource.user;

    public AssessmentAnswer() {
    }

    public AssessmentAnswer(AssessmentSession assessment, Question question, Object answerValue) {
        this.assessment = assessment;
        this.question = question;
        this.answerValue = answerValue;
    }
}
