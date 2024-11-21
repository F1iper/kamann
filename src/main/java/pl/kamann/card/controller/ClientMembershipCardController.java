package pl.kamann.card.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.card.model.MembershipCard;
import pl.kamann.card.model.MembershipCardType;
import pl.kamann.card.service.MembershipCardService;

import java.util.List;

@RestController
@RequestMapping("/api/client/membership-cards")
@RequiredArgsConstructor
public class ClientMembershipCardController {

    private final MembershipCardService membershipCardService;

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<MembershipCard>> getMembershipCardHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipCardService.getMembershipCardHistory(userId));
    }

    @PostMapping("/request")
    public ResponseEntity<MembershipCard> requestMembershipCard(
            @RequestParam Long userId,
            @RequestParam MembershipCardType type) {

        MembershipCard card = membershipCardService.purchaseMembershipCard(userId, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }
}