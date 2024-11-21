package pl.kamann.card.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.card.model.MembershipCard;
import pl.kamann.card.model.MembershipCardType;
import pl.kamann.card.service.MembershipCardService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/membership-cards")
@RequiredArgsConstructor
public class AdminMembershipCardController {

    private final MembershipCardService membershipCardService;

    @PostMapping("/purchase")
    public ResponseEntity<MembershipCard> purchaseMembershipCard(
            @RequestParam Long userId,
            @RequestParam MembershipCardType type) {

        MembershipCard card = membershipCardService.purchaseMembershipCard(userId, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PutMapping("/{cardId}/approve-payment")
    public ResponseEntity<Void> approvePayment(@PathVariable Long cardId) {
        membershipCardService.approvePayment(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/use-entrance")
    public ResponseEntity<Void> useEntrance(@PathVariable Long cardId) {
        membershipCardService.useEntrance(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<MembershipCard>> getMembershipCardHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipCardService.getMembershipCardHistory(userId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<Void> notifyExpiringCards() {
        membershipCardService.notifyExpiringCards();
        return ResponseEntity.ok().build();
    }
}