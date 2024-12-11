package pl.kamann.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MembershipCardExpirationScheduler {
    private final MembershipCardExpirationService expirationService;

    public MembershipCardExpirationScheduler(MembershipCardExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void scheduleExpirationChecks() {
        runExpirationTask();
    }

    public void runExpirationTask() {
        expirationService.expireMembershipCards();
    }
}