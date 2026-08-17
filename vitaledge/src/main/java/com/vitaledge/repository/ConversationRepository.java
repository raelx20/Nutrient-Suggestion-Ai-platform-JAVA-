package com.vitaledge.repository;

import com.vitaledge.domain.conversation.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<Conversation> findFirstByUserIdAndStatusInOrderByUpdatedAtDesc(
            UUID userId, java.util.Collection<com.vitaledge.domain.conversation.ConversationStatus> statuses);

    long countByUserId(UUID userId);
}