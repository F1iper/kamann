package pl.kamann.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.card.model.MembershipCard;
import pl.kamann.card.model.MembershipCardType;
import pl.kamann.card.repository.MembershipCardRepository;
import pl.kamann.config.exception.specific.CardNotFoundException;
import pl.kamann.config.exception.specific.UserNotFoundException;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final AppUserRepository appUserRepository;

    public MembershipCard purchaseMembershipCard(Long userId, MembershipCardType type) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setType(type);
        card.setEntrancesLeft(type.getMaxEntrances());
        card.setPurchaseDate(LocalDate.now());
        card.setValidUntil(type.getValidDays() == null ? LocalDate.now() : LocalDate.now().plusDays(type.getValidDays()));
        card.setActive(false);
        card.setPaymentApproved(false);

        return membershipCardRepository.save(card);
    }

    public void useEntrance(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Membership card not found with ID: " + cardId));

        if (!card.isActive()) {
            throw new IllegalStateException("Card is not active.");
        }

        if (card.getEntrancesLeft() <= 0) {
            throw new IllegalStateException("No entrances left on this card.");
        }

        if (card.getValidUntil().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Membership card has expired.");
        }

        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        membershipCardRepository.save(card);
    }

    public List<MembershipCard> getMembershipCardHistory(Long userId) {
        return membershipCardRepository.findByUserIdOrderByPurchaseDateDesc(userId);
    }

    public void approvePayment(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Membership card not found with ID: " + cardId));

        card.setPaymentApproved(true);
        card.setActive(true);
        membershipCardRepository.save(card);
    }

    public void notifyExpiringCards() {
        List<MembershipCard> expiringCards = membershipCardRepository.findByValidUntilBetween(
                LocalDate.now(), LocalDate.now().plusDays(3));

        for (MembershipCard card : expiringCards) {
            // todo: Trigger email notification logic here
        }
    }
}