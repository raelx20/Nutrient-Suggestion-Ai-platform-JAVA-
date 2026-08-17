package com.vitaledge.domain.assessment;

import com.vitaledge.domain.BaseEntity;
import com.vitaledge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "health_profiles",
        indexes = {
                @Index(name = "idx_healthprofile_user", columnList = "user_id"),
                @Index(name = "idx_healthprofile_assessment", columnList = "assessment_id")
        })
@Getter
@Setter
public class HealthProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AssessmentSession assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "profile_data", nullable = false)
    private Map<String, Object> profileData;

    @Column(name = "counsellor_recommended", nullable = false)
    private boolean counsellorRecommended;

    @Column(nullable = false)
    private int version = 1;

    public HealthProfile() {
    }

    public HealthProfile(AssessmentSession assessment, User user, Map<String, Object> profileData,
                         boolean counsellorRecommended) {
        this.assessment = assessment;
        this.user = user;
        this.profileData = profileData;
        this.counsellorRecommended = counsellorRecommended;
    }
}
