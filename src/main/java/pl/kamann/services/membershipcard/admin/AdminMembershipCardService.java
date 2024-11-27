package pl.kamann.services.membershipcard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.services.membershipcard.shared.SharedMembershipCardService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMembershipCardService {
    private final MembershipCardRepository membershipCardRepository;
    private final SharedMembershipCardService sharedService;

    public void approvePayment(Long cardId) {
        MembershipCard card = sharedService.findMembershipCardById(cardId);
        card.setPaid(true);
        card.setActive(true);
        membershipCardRepository.save(card);
    }

    public List<MembershipCard> getExpiringCards() {
        return membershipCardRepository.findMembershipCardsWithinDates(
                LocalDate.now(),
                LocalDate.now().plusDays(3)
        );
    }
}
