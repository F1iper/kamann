package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.ClientMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.services.client.ClientMembershipCardService;

@RestController
@RequestMapping("/api/client/membership-card")
@RequiredArgsConstructor
public class ClientMembershipCardController {

    private final ClientMembershipCardService clientMembershipCardService;

    @GetMapping("/active")
    @Operation(summary = "Retrieve the active membership card for the logged-in client.")
    public ResponseEntity<MembershipCardResponseDto> getActiveMembershipCard() {
        var response = clientMembershipCardService.getActiveCardForLoggedInUser();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase")
    @Operation(summary = "Purchase a new membership card for the logged-in client.")
    public ResponseEntity<MembershipCardResponseDto> purchaseMembershipCard(@RequestBody ClientMembershipCardRequestDto request) {
        var response = clientMembershipCardService.purchaseMembershipCard(request);
        return ResponseEntity.status(201).body(response);
    }
}
