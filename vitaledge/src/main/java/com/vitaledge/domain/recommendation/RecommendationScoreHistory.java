package com.vitaledge.domain.recommendation;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "recommendation_score_history",
        indexes = @Index(name = "idx_score_history_reco", columnList = "recommendation_id"))
@Getter
@Setter
public class RecommendationScoreHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @Column(nullable = false, length = 50)
    private String metric;

    @Column(name = "score_value", nullable = false)
    private double value;

    public RecommendationScoreHistory() {
    }

    public RecommendationScoreHistory(Recommendation recommendation, String metric, double value) {
        this.recommendation = recommendation;
        this.metric = metric;
        this.value = value;
    }
}