package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.AdminMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.services.admin.AdminMembershipCardService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/membership-card")
@RequiredArgsConstructor
public class AdminMembershipCardController {

    private final AdminMembershipCardService adminMembershipCardService;

    @PostMapping("/create/{clientId}")
    @Operation(summary = "Create a new membership card for a client.")
    public ResponseEntity<MembershipCardResponseDto> createMembershipCard(
            @PathVariable Long clientId,
            @RequestBody AdminMembershipCardRequestDto request) {
        var response = adminMembershipCardService.createCardForPromotion(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/update/{cardId}")
    @Operation(summary = "Update an existing membership card.")
    public ResponseEntity<MembershipCardResponseDto> updateMembershipCard(
            @PathVariable Long cardId,
            @RequestBody AdminMembershipCardRequestDto request) {
        var response = adminMembershipCardService.updateMembershipCard(cardId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{cardId}")
    @Operation(summary = "Delete a membership card.")
    public ResponseEntity<Void> deleteMembershipCard(@PathVariable Long cardId) {
        adminMembershipCardService.deleteMembershipCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all membership cards for a specific client.")
    public ResponseEntity<List<MembershipCardResponseDto>> getClientMembershipCards(@PathVariable Long clientId) {
        List<MembershipCardResponseDto> response = adminMembershipCardService.getClientMembershipCards(clientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve/{cardId}")
    @Operation(summary = "Approve payment and activate a membership card.")
    public ResponseEntity<Void> approvePayment(@PathVariable Long cardId) {
        adminMembershipCardService.approvePayment(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/expiring")
    @Operation(summary = "Retrieve membership cards that are expiring soon.")
    public ResponseEntity<List<MembershipCardResponseDto>> getExpiringCards() {
        List<MembershipCardResponseDto> response = adminMembershipCardService.getExpiringCards();
        return ResponseEntity.ok(response);
    }
}
