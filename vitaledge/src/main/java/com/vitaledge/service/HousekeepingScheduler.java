package com.vitaledge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Schedules the maintenance jobs that the source project ran as periodic Celery tasks.
 */
@Component
public class HousekeepingScheduler {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingScheduler.class);

    private final TokenMaintenanceService maintenanceService;

    public HousekeepingScheduler(TokenMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        long removed = maintenanceService.cleanupExpiredRefreshTokens();
        log.info("Scheduled token cleanup removed {} tokens", removed);
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void archiveOldSessions() {
        long archived = maintenanceService.archiveOldConversations(90);
        log.info("Scheduled session archival archived {} conversations", archived);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldAuditLogs() {
        long removed = maintenanceService.cleanupOldAuditLogs(365);
        log.info("Scheduled audit cleanup removed {} logs", removed);
    }
}