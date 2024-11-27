package pl.kamann.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.ClientMembershipCardRequestDto;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.services.ClientMembershipCardService;

@RestController
@RequestMapping("/api/client/membership-cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientMembershipCardController {

    private final ClientMembershipCardService clientMembershipCardService;

    @PostMapping("/request")
    public ResponseEntity<MembershipCardResponseDto> requestMembershipCard(@RequestBody ClientMembershipCardRequestDto request) {
        MembershipCardResponseDto response = clientMembershipCardService.purchaseMembershipCardForClient(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<MembershipCardResponseDto> getActiveMembershipCard() {
        MembershipCardResponseDto response = clientMembershipCardService.getActiveCardForLoggedInUser();
        return ResponseEntity.ok(response);
    }
}
