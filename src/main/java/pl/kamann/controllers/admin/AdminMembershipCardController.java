package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.AdminMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.services.admin.AdminMembershipCardService;

@RequestMapping("/api/admin/membership-cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMembershipCardController {

    private final AdminMembershipCardService adminMembershipCardService;
    private final MembershipCardMapper membershipCardMapper;

    @PostMapping("/create")
    @Operation(summary = "Create a new membership card", description = "Allows an admin to create a new membership card for promotional or regular purposes.")
    public ResponseEntity<MembershipCardResponseDto> createCard(@RequestBody AdminMembershipCardRequestDto request) {
        var card = adminMembershipCardService.createCardForPromotion(request);
        return ResponseEntity.ok(membershipCardMapper.toDto(card));
    }

    @PutMapping("/{cardId}/update")
    @Operation(summary = "Update membership card", description = "Allows an admin to update details of an existing membership card.")
    public ResponseEntity<MembershipCardResponseDto> updateCard(@PathVariable Long cardId, @RequestBody AdminMembershipCardRequestDto request) {
        var card = adminMembershipCardService.updateMembershipCard(cardId, request);
        return ResponseEntity.ok(membershipCardMapper.toDto(card));
    }

    @PostMapping("/{cardId}/approve-payment")
    @Operation(summary = "Approve membership card payment", description = "Marks a membership card's payment as approved.")
    public ResponseEntity<Void> approvePayment(@PathVariable Long cardId) {
        adminMembershipCardService.approvePayment(cardId);
        return ResponseEntity.ok().build();
    }
}