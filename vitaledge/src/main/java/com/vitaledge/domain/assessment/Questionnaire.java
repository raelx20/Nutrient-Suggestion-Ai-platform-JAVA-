package com.vitaledge.domain.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "questionnaires", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
public class Questionnaire extends com.vitaledge.domain.BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean defaultQuestionnaire;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "question_ids")
    private List<java.util.UUID> questionIds;

    @jakarta.persistence.OneToMany(mappedBy = "questionnaire")
    private List<Question> questions = new java.util.ArrayList<>();

    public Questionnaire() {
    }

    public Questionnaire(String code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }
}
