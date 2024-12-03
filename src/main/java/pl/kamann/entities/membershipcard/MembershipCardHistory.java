package pl.kamann.entities.membershipcard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kamann.entities.appuser.AppUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private MembershipCard card;

    @Enumerated(EnumType.STRING)
    private MembershipCardAction action;

    private int entriesUsed;

    private LocalDateTime actionDate;
}
