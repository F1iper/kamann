package pl.kamann.history.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.kamann.card.model.MembershipCardType;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserCardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private MembershipCardType membershipCardType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int entrances;
    private int remainingEntrances;
    private boolean paid;
}