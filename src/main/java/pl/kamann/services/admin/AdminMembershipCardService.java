package pl.kamann.services.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.AdminMembershipCardRequestDto;
import pl.kamann.entities.MembershipCard;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.MembershipCardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMembershipCardService {

    private final MembershipCardService membershipCardService;
    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardMapper membershipCardMapper;

    public void approvePayment(Long cardId) {
        MembershipCard card = membershipCardService.findMembershipCardById(cardId);
        card.setPaid(true);
        card.setActive(true);
        membershipCardRepository.save(card);
    }

    public List<MembershipCard> getExpiringCards() {
        return membershipCardService.findMembershipCardsWithinDates(
                LocalDate.now(),
                LocalDate.now().plusDays(3)
        );
    }

    @Transactional
    public MembershipCard createCardForPromotion(AdminMembershipCardRequestDto request) {
        MembershipCard card = new MembershipCard();
        card.setMembershipCardType(request.getMembershipCardType());
        card.setEntrancesLeft(request.getMembershipCardType().getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(request.getMembershipCardType().getValidDays()));
        card.setPrice(request.getPrice());
        card.setPaid(false);
        card.setActive(false);

        return membershipCardRepository.save(card);
    }

    @Transactional
    public MembershipCard updateMembershipCard(Long cardId, AdminMembershipCardRequestDto request) {
        MembershipCard card = membershipCardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        "Membership card not found.",
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND));

        if (request.getPrice() != null) {
            card.setPrice(request.getPrice());
        }
        if (request.getMembershipCardType() != null) {
            card.setMembershipCardType(request.getMembershipCardType());
            card.setEntrancesLeft(request.getMembershipCardType().getMaxEntrances());
            if (request.getMembershipCardType().getValidDays() != null) {
                if (card.getStartDate() == null) {
                    throw new ApiException(
                            "Start date is missing for the membership card.",
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            Codes.INVALID_CARD_STATE
                    );
                }
                card.setEndDate(card.getStartDate().plusDays(request.getMembershipCardType().getValidDays()));
            }
        }

        return membershipCardRepository.save(card);
    }
}
