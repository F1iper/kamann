package pl.kamann.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pl.kamann.config.rabbit.RabbitMQConfig;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.ClientMembershipCardHistory;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.events.membershipcard.MembershipCardChangeEvent;
import pl.kamann.repositories.client.ClientMembershipCardHistoryRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipCardChangeEventListener {

    private final ClientMembershipCardHistoryRepository historyRepository;
    private final EntityLookupService entityLookupService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleMembershipCardChangeEvent(MembershipCardChangeEvent event) {
        AppUser user = entityLookupService.getLoggedInUser();

        ClientMembershipCardHistory history = new ClientMembershipCardHistory();
        history.setUser(user);
        history.setMembershipCardType(MembershipCardType.valueOf(event.getChangeType().name()));
        history.setStartDate(LocalDateTime.now());
        history.setEndDate(event.getTimestamp().plusMonths(1));
        history.setEntrances(event.getRemainingEntrances());
        history.setRemainingEntrances(event.getRemainingEntrances());
        history.setPaid(true);

        historyRepository.save(history);
        log.info("Stored history for user {}: {}", user.getEmail(), history);
    }
}
