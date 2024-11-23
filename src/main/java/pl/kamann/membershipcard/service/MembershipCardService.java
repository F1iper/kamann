package pl.kamann.membershipcard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.history.model.ClientMembershipCardHistory;
import pl.kamann.history.repository.UserCardHistoryRepository;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.model.MembershipCardType;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final UserCardHistoryRepository userCardHistoryRepository;
    private final EntityLookupService lookupService;

    /**
     * Purchases a membership card for a user and logs the transaction.
     *
     * @param userId The user ID.
     * @param type   The type of membership card.
     * @return The created MembershipCard instance.
     */
    public MembershipCard purchaseMembershipCard(Long userId, MembershipCardType type) {
        AppUser user = lookupService.findUserById(userId);

        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(type);
        card.setEntrancesLeft(type.getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(type.getValidDays()));
        card.setPaid(false);
        card.setActive(false);

        MembershipCard savedCard = membershipCardRepository.save(card);

        logCardHistory(user, savedCard, "Purchase");
        return savedCard;
    }

    /**
     * Logs a card transaction in the user card history.
     *
     * @param user   The user.
     * @param card   The membership card.
     * @param action The action taken on the card.
     */
    private void logCardHistory(AppUser user, MembershipCard card, String action) {
        ClientMembershipCardHistory history = new ClientMembershipCardHistory();
        history.setUser(user);
        history.setMembershipCardType(card.getMembershipCardType());
        history.setStartDate(card.getStartDate());
        history.setEndDate(card.getEndDate());
        history.setEntrances(card.getMembershipCardType().getMaxEntrances());
        history.setRemainingEntrances(card.getEntrancesLeft());
        history.setPaid(card.isPaid());
        userCardHistoryRepository.save(history);
    }

    /**
     * Deducts an entrance from a user's membership card.
     *
     * @param cardId The membership card ID.
     */
    public void useEntrance(Long cardId) {
        MembershipCard card = findMembershipCardById(cardId);

        validateCard(card);

        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        membershipCardRepository.save(card);
    }

    /**
     * Approves the payment for a membership card, activating it for use.
     *
     * @param cardId The membership card ID.
     */
    public void approvePayment(Long cardId) {
        MembershipCard card = findMembershipCardById(cardId);

        card.setPaid(true);
        card.setActive(true);
        membershipCardRepository.save(card);
    }

    /**
     * Retrieves membership card history for a user.
     *
     * @param userId The user ID.
     * @return A list of UserCardHistory instances.
     */
    public List<MembershipCard> getMembershipCardHistory(Long userId) {
        lookupService.findUserById(userId); // Ensures the user exists
        return membershipCardRepository.findByUserIdOrderByPurchaseDateDesc(userId);
    }

    /**
     * Notifies users with cards expiring in the next 3 days.
     */
    public void notifyExpiringCards() {
        List<MembershipCard> expiringCards = membershipCardRepository.findMembershipCardsWithinDates(
                LocalDate.now(),
                LocalDate.now().plusDays(3)
        );

        expiringCards.forEach(card -> {
            String email = card.getUser().getEmail();
            System.out.println("Notify user: " + email + " about expiring card ending on " + card.getEndDate());

//             emailService.sendEmail(email, "Membership Card Expiring",
//             "Your membership card is expiring on " + card.getEndDate() + ". Please renew soon.");
        });
    }

    /**
     * Validates the state of a membership card before use.
     *
     * @param card The membership card.
     */
    private void validateCard(MembershipCard card) {
        if (!card.isActive()) {
            throw new ApiException(
                    "Membership card is not active.",
                    HttpStatus.BAD_REQUEST,
                    Codes.CARD_NOT_ACTIVE
            );
        }

        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException(
                    "No entrances left on this card.",
                    HttpStatus.BAD_REQUEST,
                    Codes.NO_ENTRANCES_LEFT
            );
        }

        if (card.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Membership card has expired.",
                    HttpStatus.BAD_REQUEST,
                    Codes.CARD_EXPIRED
            );
        }
    }

    /**
     * Finds a membership card by its ID.
     *
     * @param cardId The ID of the membership card to find.
     * @return The found MembershipCard instance.
     * @throws ApiException if the membership card is not found.
     */
    public MembershipCard findMembershipCardById(Long cardId) {
        return membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        "Membership card not found with ID: " + cardId,
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));
    }
}