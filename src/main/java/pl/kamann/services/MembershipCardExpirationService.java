package pl.kamann.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.systemevents.MembershipCardEvent;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MembershipCardExpirationService {
    private final ApplicationEventPublisher eventPublisher;
    private final MembershipCardRepository membershipCardRepository;

    public MembershipCardExpirationService(ApplicationEventPublisher eventPublisher, MembershipCardRepository membershipCardRepository) {
        this.eventPublisher = eventPublisher;
        this.membershipCardRepository = membershipCardRepository;
    }

    public void expireMembershipCards() {
        List<MembershipCard> expiringCards = membershipCardRepository.findExpiringCards(LocalDateTime.now());
        for (MembershipCard card : expiringCards) {
            card.setActive(false);
            membershipCardRepository.save(card);
            eventPublisher.publishEvent(new MembershipCardEvent(this, card.getUser().getId(), MembershipCardAction.EXPIRE));
        }
    }

    public void handleExpiration(Long userId) {
        eventPublisher.publishEvent(new MembershipCardEvent(this, userId, MembershipCardAction.EXPIRE));
    }


    public void renewMembership(Long userId) {
        MembershipCard card = membershipCardRepository.findActiveCardByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        "No active membership card found for user: " + userId,
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND));

        if (!isRenewable(card)) {
            throw new ApiException(
                    "Membership card is not eligible for renewal.",
                    HttpStatus.CONFLICT,
                    Codes.INVALID_CARD_STATE);
        }

        extendCardValidity(card);
        resetEntrances(card);

        membershipCardRepository.save(card);

        eventPublisher.publishEvent(new MembershipCardEvent(this, userId, MembershipCardAction.RENEW));
    }

    private boolean isRenewable(MembershipCard card) {
        return card.isActive() && !card.isPendingApproval() && card.getEndDate().isBefore(LocalDateTime.now().plusDays(7));
    }

    private void extendCardValidity(MembershipCard card) {
        card.setStartDate(card.getEndDate().plusSeconds(1));
        card.setEndDate(card.getEndDate().plusMonths(1));
    }

    private void resetEntrances(MembershipCard card) {
        switch (card.getMembershipCardType()) {
            case SINGLE_ENTRY:
                card.setEntrancesLeft(1);
                break;
            case MONTHLY_4:
                card.setEntrancesLeft(4);
                break;
            case MONTHLY_8:
                card.setEntrancesLeft(8);
                break;
            case MONTHLY_12:
                card.setEntrancesLeft(12);
                break;
            default:
                throw new ApiException(
                        "Unknown membership card type: " + card.getMembershipCardType(),
                        HttpStatus.CONFLICT,
                        Codes.UNKNOWN_CARD_TYPE);
        }
    }
}