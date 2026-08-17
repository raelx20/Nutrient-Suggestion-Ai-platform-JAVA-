package com.vitaledge.domain.recommendation;

import com.vitaledge.domain.BaseEntity;
import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.product.Product;
import com.vitaledge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "recommendations",
        indexes = {
                @Index(name = "idx_reco_user", columnList = "user_id"),
                @Index(name = "idx_reco_assessment", columnList = "assessment_id"),
                @Index(name = "idx_reco_product", columnList = "product_id")
        })
@Getter
@Setter
public class Recommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AssessmentSession assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id")
    private HealthProfile healthProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "recommended_at", nullable = false)
    private LocalDateTime recommendedAt = LocalDateTime.now();

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false)
    private int rank;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_safe", nullable = false)
    private boolean safe;

    @Column(name = "is_excluded", nullable = false)
    private boolean excluded;

    public Recommendation() {
    }

    public Recommendation(User user, AssessmentSession assessment, HealthProfile healthProfile,
                          Product product, String reason, double confidence, int rank,
                          boolean safe, boolean excluded) {
        this.user = user;
        this.assessment = assessment;
        this.healthProfile = healthProfile;
        this.product = product;
        this.reason = reason;
        this.confidence = confidence;
        this.rank = rank;
        this.safe = safe;
        this.excluded = excluded;
    }
}