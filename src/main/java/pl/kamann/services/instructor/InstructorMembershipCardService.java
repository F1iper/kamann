package pl.kamann.services.instructor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.MembershipCard;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.utility.EntityLookupService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorMembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final EntityLookupService lookupService;
    private final MembershipCardMapper membershipCardMapper;

    public List<MembershipCardResponseDto> getClientMembershipCards(Long clientId) {
        AppUser client = lookupService.findUserById(clientId);
        List<MembershipCard> cards = membershipCardRepository.findAllByUser(client);

        if (cards.isEmpty()) {
            throw new ApiException(
                    "No membership cards found for the client.",
                    HttpStatus.NOT_FOUND,
                    Codes.CARD_NOT_FOUND
            );
        }

        return cards.stream()
                .map(membershipCardMapper::toDto)
                .collect(Collectors.toList());
    }

    public void validateMembershipForEvent(Long clientId) {
        MembershipCard activeCard = membershipCardRepository.findActiveCardByUserId(clientId)
                .orElseThrow(() -> new ApiException(
                        "Client does not have a valid membership card for event participation.",
                        HttpStatus.BAD_REQUEST,
                        Codes.CARD_NOT_ACTIVE
                ));

        if (activeCard.getEntrancesLeft() <= 0) {
            throw new ApiException(
                    "The membership card for the client has no remaining entrances.",
                    HttpStatus.BAD_REQUEST,
                    Codes.NO_ENTRANCES_LEFT
            );
        }
    }
}
