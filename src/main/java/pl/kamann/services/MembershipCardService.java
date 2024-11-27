package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.MembershipCardRequestDto;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.MembershipCard;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipCardService {

    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardMapper membershipCardMapper;

    public MembershipCard findMembershipCardById(Long cardId) {
        return membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        "Membership card not found with ID: " + cardId,
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));
    }

    public MembershipCard findActiveCardByUserId(Long userId) {
        return membershipCardRepository.findActiveCardByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        "No active membership card found for user ID: " + userId,
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));
    }

    public MembershipCard createMembershipCard(MembershipCardRequestDto request, AppUser user) {
        MembershipCard card = membershipCardMapper.toEntity(request, user);
        return membershipCardRepository.save(card);
    }

    public void validateMembershipCard(MembershipCard card) {
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

    public List<MembershipCard> findMembershipCardsWithinDates(LocalDate startDate, LocalDate endDate) {
        return membershipCardRepository.findMembershipCardsWithinDates(startDate, endDate);
    }
}
