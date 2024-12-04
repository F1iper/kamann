package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipCardExpirationScheduler {

    private final MembershipCardExpirationService expirationService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runExpirationTask() {
        expirationService.expireMembershipCards();
    }
}