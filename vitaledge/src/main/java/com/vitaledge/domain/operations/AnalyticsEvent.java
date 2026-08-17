package com.vitaledge.domain.operations;

import com.vitaledge.domain.BaseEntity;
import com.vitaledge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "analytics_events",
        indexes = {
                @Index(name = "idx_analytics_event_type", columnList = "event_type"),
                @Index(name = "idx_analytics_created", columnList = "created_at")
        })
@Getter
@Setter
public class AnalyticsEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "event_data")
    private Map<String, Object> eventData;

    public AnalyticsEvent() {
    }

    public AnalyticsEvent(User user, String eventType, Map<String, Object> eventData) {
        this.user = user;
        this.eventType = eventType;
        this.eventData = eventData;
    }
}
