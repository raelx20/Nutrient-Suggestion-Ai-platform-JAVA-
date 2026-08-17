package com.vitaledge.service;

import com.vitaledge.common.exception.ApiException;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.conversation.Conversation;
import com.vitaledge.domain.conversation.ConversationStatus;
import com.vitaledge.domain.conversation.Message;
import com.vitaledge.domain.conversation.MessageRole;
import com.vitaledge.domain.conversation.MessageType;
import com.vitaledge.domain.conversation.SessionType;
import com.vitaledge.domain.user.User;
import com.vitaledge.repository.ConversationRepository;
import com.vitaledge.repository.HealthProfileRepository;
import com.vitaledge.repository.MessageRepository;
import com.vitaledge.repository.UserRepository;
import com.vitaledge.web.dto.conversation.ChatRequest;
import com.vitaledge.web.dto.conversation.ChatResponse;
import com.vitaledge.web.dto.conversation.ConversationResponse;
import com.vitaledge.web.dto.conversation.MessageResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Conversation/messaging service. Ports the source project's chat router and message
 * persistence: session typing, user + assistant message storage, and AI-driven replies.
 */
@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final AIOrchestratorService orchestrator;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository,
                       HealthProfileRepository healthProfileRepository,
                       AIOrchestratorService orchestrator) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.orchestrator = orchestrator;
    }

    @Transactional
    public ChatResponse sendMessage(UUID userId, UUID conversationId, ChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));

        SessionType sessionType = parseSessionType(request.sessionType());
        Conversation conversation;
        if (conversationId != null) {
            conversation = requireOwned(userId, conversationId);
        } else {
            conversation = findOrCreateConversation(user, sessionType);
        }

        Message userMessage = messageRepository.save(
                new Message(conversation, MessageRole.user, sanitize(request.message()), MessageType.text));

        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        HealthProfile latestProfile = healthProfileRepository
                .findFirstByUserIdOrderByVersionDesc(userId).orElse(null);

        String reply = orchestrator.handle(sessionType, request.message(), history,
                latestProfile, List.of());

        Message assistantMessage = messageRepository.save(
                new Message(conversation, MessageRole.assistant, reply, MessageType.text));
        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversation.setSessionType(sessionType);
        conversationRepository.save(conversation);

        return new ChatResponse(
                conversation.getId(),
                MessageResponse.from(userMessage),
                MessageResponse.from(assistantMessage),
                sessionType.name(),
                suggestedActions(sessionType));
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID userId, UUID conversationId) {
        Conversation conversation = requireOwned(userId, conversationId);
        List<MessageResponse> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream().map(MessageResponse::from).toList();
        return new ConversationResponse(
                conversation.getId(),
                conversation.getStatus().name(),
                conversation.getSessionType().name(),
                conversation.getStartedAt(),
                conversation.getUpdatedAt(),
                messages);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(c -> new ConversationResponse(
                        c.getId(), c.getStatus().name(), c.getSessionType().name(),
                        c.getStartedAt(), c.getUpdatedAt(), List.of()))
                .toList();
    }

    private Conversation findOrCreateConversation(User user, SessionType sessionType) {
        Optional<Conversation> active = conversationRepository.findFirstByUserIdAndStatusInOrderByUpdatedAtDesc(
                user.getId(), List.of(ConversationStatus.active, ConversationStatus.awaiting_assessment));
        return active.orElseGet(() -> {
            Conversation created = new Conversation(user, sessionType);
            created.setUpdatedAt(java.time.LocalDateTime.now());
            return conversationRepository.save(created);
        });
    }

    private Conversation requireOwned(UUID userId, UUID conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CONVERSATION_NOT_FOUND",
                        "Conversation not found."));
    }

    private SessionType parseSessionType(String value) {
        if (value == null || value.isBlank()) {
            return SessionType.general;
        }
        try {
            return SessionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return SessionType.general;
        }
    }

    private String sanitize(String message) {
        String trimmed = message == null ? "" : message.trim();
        return trimmed.length() > 4000 ? trimmed.substring(0, 4000) : trimmed;
    }

    private List<ChatResponse.Action> suggestedActions(SessionType sessionType) {
        return switch (sessionType) {
            case welcome -> List.of(
                    new ChatResponse.Action("start_assessment", "Start an assessment",
                            Map.of("url", "/api/v1/assessments/start")));
            case recommendation -> List.of(
                    new ChatResponse.Action("view_recommendations", "View my recommendations",
                            Map.of("url", "/api/v1/assessments/recommendations")));
            default -> List.of();
        };
    }
}