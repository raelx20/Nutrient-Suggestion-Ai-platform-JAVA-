package com.vitaledge.domain.conversation;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "messages",
        indexes = @Index(name = "idx_message_conversation", columnList = "conversation_id"))
@Getter
@Setter
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, length = 20000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private MessageType messageType = MessageType.text;

    public Message() {
    }

    public Message(Conversation conversation, MessageRole role, String content, MessageType messageType) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.messageType = messageType;
    }
}