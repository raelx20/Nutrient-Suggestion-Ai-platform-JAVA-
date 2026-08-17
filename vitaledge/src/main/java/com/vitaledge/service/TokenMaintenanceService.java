package com.vitaledge.service;

import com.vitaledge.domain.conversation.Conversation;
import com.vitaledge.domain.conversation.ConversationStatus;
import com.vitaledge.repository.AuditLogRepository;
import com.vitaledge.repository.ConversationRepository;
import com.vitaledge.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodic maintenance jobs. Ports the source project's Celery maintenance tasks:
 * {@code cleanup_expired_tokens} and {@code cleanup_old_sessions}.
 */
@Service
public class TokenMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(TokenMaintenanceService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final ConversationRepository conversationRepository;
    private final AuditLogRepository auditLogRepository;

    public TokenMaintenanceService(RefreshTokenRepository refreshTokenRepository,
                                   ConversationRepository conversationRepository,
                                   AuditLogRepository auditLogRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.conversationRepository = conversationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public long cleanupExpiredRefreshTokens() {
        long before = refreshTokenRepository.count();
        long removed = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now().minusDays(1));
        long after = before - removed;
        log.info("Refresh token cleanup: removed {}, remaining {}", removed, after);
        return removed;
    }

    @Transactional
    public long archiveOldConversations(int maxActiveDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(maxActiveDays);
        List<Conversation> stale = conversationRepository.findAll().stream()
                .filter(c -> (c.getStatus() == ConversationStatus.active
                        || c.getStatus() == ConversationStatus.awaiting_assessment)
                        && c.getUpdatedAt() != null
                        && c.getUpdatedAt().isBefore(cutoff))
                .toList();
        stale.forEach(c -> {
            c.archive();
            conversationRepository.save(c);
        });
        log.info("Conversation archival: archived {} stale conversations", stale.size());
        return stale.size();
    }

    @Transactional
    public long cleanupOldAuditLogs(int maxAgeDays) {
        long removed = auditLogRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(maxAgeDays));
        log.info("Audit log cleanup: removed {}", removed);
        return removed;
    }
}