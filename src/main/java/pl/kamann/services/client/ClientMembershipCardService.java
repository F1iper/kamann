package pl.kamann.services.client;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.MembershipCardCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.MembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientMembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardService membershipCardService;
    private final EntityLookupService lookupService;

    @Transactional
    public MembershipCard requestMembershipCard(Long cardId) {
        var loggedInUser = lookupService.getLoggedInUser();
        var client = lookupService.findUserById(loggedInUser.getId());

        if (membershipCardRepository.findActiveCardByUserId(loggedInUser.getId()).isPresent()) {
            throw new ApiException(
                    "Client already has an active membership card.",
                    HttpStatus.BAD_REQUEST,
                    MembershipCardCodes.CARD_ALREADY_EXISTS.name()
            );
        }

        var cardTemplate = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        "Membership card not found.",
                        HttpStatus.NOT_FOUND,
                        MembershipCardCodes.CARD_NOT_FOUND.name()
                ));

        MembershipCard clientCard = MembershipCard.builder()
                .user(client)
                .membershipCardType(cardTemplate.getMembershipCardType())
                .entrancesLeft(cardTemplate.getMembershipCardType().getMaxEntrances())
                .price(cardTemplate.getPrice())
                .startDate(null)
                .endDate(null)
                .paid(false)
                .active(false)
                .pendingApproval(true)
                .build();

        return membershipCardRepository.save(clientCard);
    }

    public List<MembershipCard> getAvailableMembershipCards() {
        return membershipCardRepository.findByUserIsNullAndActiveFalse();
    }

    public MembershipCard getActiveCard(Long clientId) {
        List<MembershipCard> activeCards = membershipCardRepository.findByUserIdAndActiveTrue(clientId);

        if (activeCards.isEmpty()) {
            throw new ApiException(
                    "No active membership card found.",
                    HttpStatus.NOT_FOUND,
                    MembershipCardCodes.CARD_NOT_ACTIVE.name()
            );
        }

        if (activeCards.size() > 1) {
            throw new ApiException(
                    "Multiple active membership cards found.",
                    HttpStatus.CONFLICT,
                    MembershipCardCodes.MULTIPLE_ACTIVE_CARDS.name()
            );
        }

        return activeCards.get(0);
    }

    @Transactional
    public MembershipCard deductEntry(Long clientId) {
        MembershipCard activeCard = getActiveCard(clientId);

        if (activeCard.getEntrancesLeft() <= 0) {
            throw new ApiException(
                    "The membership card has no remaining entrances.",
                    HttpStatus.BAD_REQUEST,
                    MembershipCardCodes.NO_ENTRANCES_LEFT.name()
            );
        }

        activeCard.setEntrancesLeft(activeCard.getEntrancesLeft() - 1);
        membershipCardRepository.save(activeCard);

        membershipCardService.logAction(activeCard, activeCard.getUser(), MembershipCardAction.USED, 1);


        return activeCard;
    }
}
