package pl.kamann.systemevents.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.systemevents.MembershipCardEvent;

@Service
public class MembershipCardEventListener {

    @EventListener
    public void onMembershipCardEvent(MembershipCardEvent event) {
        if (MembershipCardAction.EXPIRE.equals(event.getAction())) {
            processExpiration(event.getUserId());
        } else if (MembershipCardAction.RENEW.equals(event.getAction())) {
            processRenewal(event.getUserId());
        }
    }

    private void processExpiration(Long userId) {
        //todo log
        System.out.println("Processing expiration for user: " + userId);
    }

    private void processRenewal(Long userId) {
        //todo log
        System.out.println("Processing renewal for user: " + userId);
    }
}