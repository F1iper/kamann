package pl.kamann.controllers.client;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.ClientMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.services.client.ClientMembershipCardService;

@RestController
@RequestMapping("/api/client/membership-cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientMembershipCardController {

    private final ClientMembershipCardService clientMembershipCardService;

    @PostMapping("/request")
    @Operation(summary = "Request a new membership card", description = "Allows a client to request a new membership card based on the selected type.")
    public ResponseEntity<MembershipCardResponseDto> requestMembershipCard(@RequestBody ClientMembershipCardRequestDto request) {
        var response = clientMembershipCardService.purchaseMembershipCardForClient(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Retrieve active membership card", description = "Fetches the active membership card for the currently logged-in client.")
    public ResponseEntity<MembershipCardResponseDto> getActiveMembershipCard() {
        var response = clientMembershipCardService.getActiveCardForLoggedInUser();
        return ResponseEntity.ok(response);
    }
}
