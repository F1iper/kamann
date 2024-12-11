package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.services.MembershipCardExpirationService;
import pl.kamann.services.admin.AdminMembershipCardService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/membership-cards")
@RequiredArgsConstructor
public class AdminMembershipCardController {

    private final AdminMembershipCardService adminMembershipCardService;
    private final MembershipCardExpirationService membershipCardExpirationService;

    @PostMapping("/create")
    @Operation(summary = "Create a new predefined membership card.")
    public ResponseEntity<Void> createMembershipCard(
            @RequestParam MembershipCardType type,
            @RequestParam BigDecimal price) {
        adminMembershipCardService.createMembershipCard(type, price);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/approve/{cardId}")
    @Operation(summary = "Approve a client's membership card request.")
    public ResponseEntity<Void> approveClientCardRequest(@PathVariable Long cardId) {
        adminMembershipCardService.approveClientCardRequest(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/renew/{userId}")
    @Operation(summary = "Renew a user's membership card.")
    public ResponseEntity<Void> renewMembershipCard(@PathVariable Long userId) {
        membershipCardExpirationService.renewMembership(userId);
        return ResponseEntity.ok().build();
    }
}
