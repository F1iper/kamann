package pl.kamann.services.instructor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.MembershipCardCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.MembershipCardResponse;
import pl.kamann.dtos.MembershipCardValidationResponse;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.MembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorMembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardService membershipCardService;
    private final MembershipCardMapper membershipCardMapper;
    private final EntityLookupService lookupService;

    public List<MembershipCardResponse> getClientMembershipCards(Long clientId) {
        AppUser client = lookupService.findUserById(clientId);
        List<MembershipCard> cards = membershipCardRepository.findAllByUser(client);

        if (cards.isEmpty()) {
            throw new ApiException(
                    "No membership cards found for the client.",
                    HttpStatus.NOT_FOUND,
                    MembershipCardCodes.CARD_NOT_FOUND.name()
            );
        }

        return cards.stream().map(membershipCardMapper::toResponse).toList();
    }

    public MembershipCardValidationResponse validateMembershipForEvent(Long clientId, Long eventId) {
        MembershipCard activeCard = membershipCardService.validateActiveCard(clientId);

        membershipCardService.useEntrance(activeCard);
        membershipCardService.logAction(activeCard, activeCard.getUser(), MembershipCardAction.USED, 1);

        return new MembershipCardValidationResponse(
                true,
                "Membership card validated successfully for event: " + eventId,
                clientId,
                eventId,
                activeCard.getEntrancesLeft()
        );
    }
}
