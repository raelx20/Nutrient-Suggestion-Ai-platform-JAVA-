package com.vitaledge.domain.assessment;

import com.vitaledge.domain.BaseEntity;
import com.vitaledge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "assessment_sessions",
        indexes = {
                @Index(name = "idx_assessment_user_id", columnList = "user_id"),
                @Index(name = "idx_assessment_status", columnList = "status"),
                @Index(name = "idx_assessment_created", columnList = "created_at")
        })
@Getter
@Setter
public class AssessmentSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id")
    private Questionnaire questionnaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssessmentStatus status = AssessmentStatus.in_progress;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "abandoned_at")
    private LocalDateTime abandonedAt;

    @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY)
    private List<AssessmentAnswer> answers = new ArrayList<>();

    @OneToOne(mappedBy = "assessment", fetch = FetchType.LAZY)
    private HealthProfile healthProfile;

    @Column(name = "current_question_key", length = 80)
    private String currentQuestionKey;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    public AssessmentSession() {
    }

    public AssessmentSession(User user, Questionnaire questionnaire) {
        this.user = user;
        this.questionnaire = questionnaire;
    }

    public void markCompleted() {
        this.status = AssessmentStatus.completed;
        this.completedAt = LocalDateTime.now();
    }

    public void markAbandoned() {
        this.status = AssessmentStatus.abandoned;
        this.abandonedAt = LocalDateTime.now();
    }
}