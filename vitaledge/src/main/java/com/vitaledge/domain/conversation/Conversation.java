package com.vitaledge.domain.conversation;

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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(name = "idx_conversation_user", columnList = "user_id"),
                @Index(name = "idx_conversation_status", columnList = "status"),
                @Index(name = "idx_conversation_updated", columnList = "updated_at")
        })
@Getter
@Setter
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsellor_id")
    private Counsellor counsellor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConversationStatus status = ConversationStatus.active;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 30)
    private SessionType sessionType = SessionType.welcome;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "context_data")
    private Map<String, Object> contextData;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    public Conversation() {
    }

    public Conversation(User user, SessionType sessionType) {
        this.user = user;
        this.sessionType = sessionType;
    }

    public void archive() {
        this.status = ConversationStatus.archived;
        this.endedAt = LocalDateTime.now();
    }
}
