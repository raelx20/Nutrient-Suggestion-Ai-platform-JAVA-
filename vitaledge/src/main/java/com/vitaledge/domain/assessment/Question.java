package com.vitaledge.domain.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"questionnaire_id", "question_key"}))
@Getter
@Setter
public class Question extends com.vitaledge.domain.BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @Column(name = "question_key", nullable = false, length = 80)
    private String key;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "question_type", nullable = false, length = 30)
    private String type;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "options")
    private List<String> options;

    @Column(name = "question_order", nullable = false)
    private int orderIndex;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "validation_rules")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private java.util.Map<String, Object> validationRules;

    public Question() {
    }

    public Question(String key, String text, String type, int orderIndex, boolean required) {
        this.key = key;
        this.text = text;
        this.type = type;
        this.orderIndex = orderIndex;
        this.required = required;
    }
}
