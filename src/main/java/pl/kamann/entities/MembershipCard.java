package pl.kamann.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipCardType membershipCardType;

    @Column(nullable = false)
    @Min(0)
    private int entrancesLeft;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean pendingApproval = false;

    @Column(nullable = false)
    private BigDecimal price;

    // todo: is required? need client opinion
    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @PrePersist
    private void setDefaultPurchaseDate() {
        if (this.purchaseDate == null) {
            this.purchaseDate = LocalDateTime.now();
        }
    }
}
