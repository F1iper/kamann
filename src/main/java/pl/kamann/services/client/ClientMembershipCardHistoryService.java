package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.entities.membershipcard.MembershipCardHistory;
import pl.kamann.repositories.client.ClientMembershipCardHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientMembershipCardHistoryService {

    private final ClientMembershipCardHistoryRepository membershipCardHistoryRepository;

    public void logMembershipCardAction(AppUser user, MembershipCard card, MembershipCardAction action, int entriesUsed) {
        var history = new MembershipCardHistory();
        history.setUser(user);
        history.setCard(card);
        history.setAction(action);
        history.setEntriesUsed(entriesUsed);
        history.setActionDate(LocalDateTime.now());

        membershipCardHistoryRepository.save(history);
    }
}
