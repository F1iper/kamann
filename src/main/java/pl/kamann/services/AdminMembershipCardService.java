package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.entities.MembershipCard;
import pl.kamann.repositories.MembershipCardRepository;

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
