package pl.kamann.services;

import org.springframework.stereotype.Component;

@Component
public class MembershipCardExpirationScheduler {
    private final MembershipCardExpirationService expirationService;

    public MembershipCardExpirationScheduler(MembershipCardExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    public void runExpirationTask() {
        expirationService.expireMembershipCards();
    }
}