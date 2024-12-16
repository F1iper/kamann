package pl.kamann.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.kamann.events.membershipcard.MembershipCardChangeEvent;

@Component
@Slf4j
public class MembershipCardChangeEventListener {

    @RabbitListener(queues = "membership-card-queue")
    public void handleMembershipCardChangeEvent(MembershipCardChangeEvent event) {
        log.info("Received MembershipCardChangeEvent: {}", event);

        switch (event.getChangeType()) {
            case USED:
                log.info("Membership card {} used by user {}. Remaining entrances: {}",
                        event.getMembershipCardId(),
                        event.getUserId(),
                        event.getRemainingEntrances());
                break;

            case EXPIRE:
                log.info("Membership card {} expired for user {}", 
                        event.getMembershipCardId(),
                        event.getUserId());
                break;

            case RENEW:
                log.info("Membership card {} renewed for user {}", 
                        event.getMembershipCardId(),
                        event.getUserId());
                break;

            default:
                log.warn("Unknown event type: {}", event.getChangeType());
        }
    }
}
