package pl.kamann.services.membershipcard.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SharedMembershipCardService {
    private final MembershipCardRepository membershipCardRepository;
    private final EntityLookupService lookupService;

    public MembershipCard findMembershipCardById(Long cardId) {
        return membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        "Membership card not found with ID: " + cardId,
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));
    }

    public void validateCard(MembershipCard card) {
        if (!card.isActive()) {
            throw new ApiException("Membership card is not active.", HttpStatus.BAD_REQUEST, Codes.CARD_NOT_ACTIVE);
        }

        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException("No entrances left on this card.", HttpStatus.BAD_REQUEST, Codes.NO_ENTRANCES_LEFT);
        }

        if (card.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Membership card has expired.", HttpStatus.BAD_REQUEST, Codes.CARD_EXPIRED);
        }
    }
}
