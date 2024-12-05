package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig
class MembershipCardExpirationSchedulerTest {

    private final MembershipCardExpirationService expirationService = Mockito.mock(MembershipCardExpirationService.class);

    private final MembershipCardExpirationScheduler scheduler = new MembershipCardExpirationScheduler(expirationService);

    @Test
    void runExpirationTask_shouldInvokeExpirationService() {
        scheduler.runExpirationTask();

        verify(expirationService, times(1)).expireMembershipCards();
    }
}