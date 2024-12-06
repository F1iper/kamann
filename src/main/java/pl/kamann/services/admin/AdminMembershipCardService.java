package pl.kamann.services.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.repositories.MembershipCardRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMembershipCardService {

    private final MembershipCardRepository membershipCardRepository;

    public MembershipCard createMembershipCard(MembershipCardType type, BigDecimal price) {
        MembershipCard card = MembershipCard.builder()
                .membershipCardType(type)
                .entrancesLeft(type.getMaxEntrances())
                .startDate(null)
                .endDate(null)
                .price(price)
                .paid(false)
                .active(false)
                .build();

        return membershipCardRepository.save(card);
    }

    public MembershipCard approveClientCardRequest(Long cardId) {
        var card = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        "Membership card not found.",
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));

        card.setPaid(true);
        card.setActive(true);
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(card.getMembershipCardType().getValidDays()));

        return membershipCardRepository.save(card);
    }
}
