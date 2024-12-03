package pl.kamann.controllers.instructor;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.MembershipCardResponseDto;
import pl.kamann.services.instructor.InstructorMembershipCardService;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/membership-card")
@RequiredArgsConstructor
public class InstructorMembershipCardController {

    private final InstructorMembershipCardService instructorMembershipCardService;

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Retrieve all membership cards for a specific client.")
    public ResponseEntity<List<MembershipCardResponseDto>> getClientMembershipCards(@PathVariable Long clientId) {
        List<MembershipCardResponseDto> response = instructorMembershipCardService.getClientMembershipCards(clientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate/{clientId}")
    @Operation(summary = "Validate a client's membership card for event participation.")
    public ResponseEntity<Void> validateMembershipForEvent(@PathVariable Long clientId) {
        instructorMembershipCardService.validateMembershipForEvent(clientId);
        return ResponseEntity.ok().build();
    }
}
