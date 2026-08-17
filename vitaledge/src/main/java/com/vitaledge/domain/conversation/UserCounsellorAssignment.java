package com.vitaledge.domain.conversation;

import com.vitaledge.domain.BaseEntity;
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
        name = "user_counsellor_assignments",
        indexes = {
                @Index(name = "idx_assignment_user", columnList = "user_id"),
                @Index(name = "idx_assignment_counsellor", columnList = "counsellor_id")
        })
@Getter
@Setter
public class UserCounsellorAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsellor_id", nullable = false)
    private Counsellor counsellor;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public UserCounsellorAssignment() {
    }

    public UserCounsellorAssignment(User user, Counsellor counsellor) {
        this.user = user;
        this.counsellor = counsellor;
    }
}