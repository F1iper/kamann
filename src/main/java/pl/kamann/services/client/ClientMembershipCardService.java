package pl.kamann.services.client;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.ClientMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientMembershipCardService {

    private final EntityLookupService lookupService;
    private final MembershipCardRepository membershipCardRepository;
    private final MembershipCardMapper membershipCardMapper;
    private final ClientMembershipCardHistoryService membershipCardHistoryService;

    public MembershipCardResponseDto getActiveCardForLoggedInUser() {
        var user = lookupService.getLoggedInUser();

        var activeCard = membershipCardRepository.findActiveCardByUserId(user.getId())
                .orElseThrow(() -> new ApiException(
                        "No active membership card found for the logged-in user.",
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));

        return membershipCardMapper.toDto(activeCard);
    }

    public MembershipCardResponseDto purchaseMembershipCard(ClientMembershipCardRequestDto request) {
        var user = lookupService.getLoggedInUser();

        if (membershipCardRepository.existsActiveCardByUserId(user.getId())) {
            throw new ApiException(
                    "Cannot purchase a new membership card while an active one exists.",
                    HttpStatus.CONFLICT,
                    Codes.CARD_ALREADY_EXISTS
            );
        }

        var card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(request.getMembershipCardType());
        card.setEntrancesLeft(request.getMembershipCardType().getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(request.getMembershipCardType().getValidDays()));
        card.setPaid(false);
        card.setActive(false);

        var savedCard = membershipCardRepository.save(card);
        membershipCardHistoryService.logMembershipCardAction(user, savedCard, MembershipCardAction.PURCHASED, 1);

        return membershipCardMapper.toDto(savedCard);
    }

    @Transactional
    public void deductEntry(Long userId) {
        var user = lookupService.findUserById(userId);
        var activeCard = membershipCardRepository.findActiveCardByUserId(user.getId())
                .orElseThrow(() -> new ApiException(
                        "No active membership card found for the user.",
                        HttpStatus.NOT_FOUND,
                        Codes.CARD_NOT_FOUND
                ));

        if (activeCard.getEntrancesLeft() <= 0) {
            throw new ApiException(
                    "No remaining entries on the membership card.",
                    HttpStatus.BAD_REQUEST,
                    Codes.NO_ENTRANCES_LEFT
            );
        }

        activeCard.setEntrancesLeft(activeCard.getEntrancesLeft() - 1);
        membershipCardRepository.save(activeCard);

        membershipCardHistoryService.logMembershipCardAction(user, activeCard, MembershipCardAction.USED, 1); // Log action
    }
}
