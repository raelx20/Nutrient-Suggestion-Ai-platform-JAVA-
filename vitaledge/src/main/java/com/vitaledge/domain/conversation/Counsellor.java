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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "counsellors",
        indexes = @Index(name = "idx_counsellor_user", columnList = "user_id"))
@Getter
@Setter
public class Counsellor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String specialty;

    @Column(length = 255)
    private String qualification;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Counsellor() {
    }

    public Counsellor(User user, String specialty, String qualification) {
        this.user = user;
        this.specialty = specialty;
        this.qualification = qualification;
    }
}