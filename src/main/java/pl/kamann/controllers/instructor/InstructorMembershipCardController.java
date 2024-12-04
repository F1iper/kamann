package pl.kamann.controllers.instructor;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.MembershipCardResponse;
import pl.kamann.services.instructor.InstructorMembershipCardService;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/membership-cards")
@RequiredArgsConstructor
public class InstructorMembershipCardController {

    private final InstructorMembershipCardService instructorMembershipCardService;

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Retrieve all membership cards for a specific client assigned to the instructor.")
    public ResponseEntity<List<MembershipCardResponse>> getClientMembershipCards(@PathVariable Long clientId) {
        List<MembershipCardResponse> response = instructorMembershipCardService.getClientMembershipCards(clientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate/{clientId}/{eventId}")
    @Operation(summary = "Validate a client's membership card for a specific event.")
    public ResponseEntity<String> validateMembershipForEvent(
            @PathVariable Long clientId,
            @PathVariable Long eventId) {
        String validationResult = instructorMembershipCardService.validateMembershipForEvent(clientId, eventId);
        return ResponseEntity.ok(validationResult);
    }
}
