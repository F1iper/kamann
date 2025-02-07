package pl.kamann.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.MembershipCardCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.entities.membershipcard.MembershipCardHistory;
import pl.kamann.repositories.MembershipCardRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipCardService {

    private final MembershipCardRepository membershipCardRepository;

    public MembershipCard validateActiveCard(Long clientId) {
        return membershipCardRepository.findActiveCardByUserId(clientId)
                .orElseThrow(() -> new ApiException(
                        "No active membership card found.",
                        HttpStatus.NOT_FOUND,
                        MembershipCardCodes.CARD_NOT_ACTIVE.name()
                ));
    }

    public void logAction(MembershipCard card, AppUser user, MembershipCardAction action, int entriesUsed) {
        if (card == null || user == null || action == null) {
            throw new IllegalArgumentException("Card, User, and Action must not be null.");
        }

        if (action == MembershipCardAction.USED && entriesUsed <= 0) {
            throw new IllegalArgumentException("Entries used must be greater than 0 for USED action.");
        }

        var history = new MembershipCardHistory();
        history.setCard(card);
        history.setUser(user);
        history.setAction(action);
        history.setEntriesUsed(action == MembershipCardAction.USED ? entriesUsed : 0);
        history.setActionDate(LocalDateTime.now());

    }

    @Transactional
    public MembershipCard useEntrance(MembershipCard card) {
        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException(
                    "No remaining entrances on this membership card.",
                    HttpStatus.BAD_REQUEST,
                    MembershipCardCodes.NO_ENTRANCES_LEFT.name()
            );
        }

        card.setEntrancesLeft(card.getEntrancesLeft() - 1);

        logAction(card, card.getUser(), MembershipCardAction.USED, 1);

        return membershipCardRepository.save(card);
    }

    @Transactional
    public void expireCard(MembershipCard card) {
        log.debug("Before setActive(false): card active status = {}", card.isActive());
        card.setActive(false);
        log.debug("After setActive(false): card active status = {}", card.isActive());

        MembershipCardHistory history = new MembershipCardHistory();
        history.setCard(card);
        history.setAction(MembershipCardAction.EXPIRE);
        history.setActionDate(LocalDateTime.now());
        history.setUser(card.getUser());

        membershipCardRepository.save(card);
    }


}
