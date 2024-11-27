package pl.kamann.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.AdminMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.entities.MembershipCard;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.services.AdminMembershipCardService;

@RequestMapping("/api/admin/membership-cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMembershipCardController {

    private final AdminMembershipCardService adminMembershipCardService;
    private final MembershipCardMapper membershipCardMapper;

    @PostMapping("/create")
    public ResponseEntity<MembershipCardResponseDto> createCard(@RequestBody AdminMembershipCardRequestDto request) {
        MembershipCard card = adminMembershipCardService.createCardForPromotion(request);
        return ResponseEntity.ok(membershipCardMapper.toDto(card));
    }

    @PutMapping("/{cardId}/update")
    public ResponseEntity<MembershipCardResponseDto> updateCard(@PathVariable Long cardId, @RequestBody AdminMembershipCardRequestDto request) {
        MembershipCard card = adminMembershipCardService.updateMembershipCard(cardId, request);
        return ResponseEntity.ok(membershipCardMapper.toDto(card));
    }

    @PostMapping("/{cardId}/approve-payment")
    public ResponseEntity<Void> approvePayment(@PathVariable Long cardId) {
        adminMembershipCardService.approvePayment(cardId);
        return ResponseEntity.ok().build();
    }
}