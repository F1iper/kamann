package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.services.client.ClientMembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@RestController
@RequestMapping("/api/client/membership-cards")
@RequiredArgsConstructor
public class ClientMembershipCardController {

    private final ClientMembershipCardService clientMembershipCardService;
    private final EntityLookupService lookupService;

    @GetMapping("/available")
    @Operation(summary = "Fetch all available predefined membership cards.")
    public ResponseEntity<List<MembershipCard>> getAvailableMembershipCards() {
        var cards = clientMembershipCardService.getAvailableMembershipCards();
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/request")
    @Operation(summary = "Request a predefined membership card.")
    public ResponseEntity<Void> requestMembershipCard(
            @RequestParam Long cardId) {
        clientMembershipCardService.requestMembershipCard(cardId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/active")
    @Operation(summary = "Get the currently active membership card for the logged-in user.")
    public ResponseEntity<MembershipCard> getActiveMembershipCard() {
        MembershipCard card = clientMembershipCardService.getActiveCard(lookupService.getLoggedInUser().getId());
        return ResponseEntity.ok(card);
    }
}
