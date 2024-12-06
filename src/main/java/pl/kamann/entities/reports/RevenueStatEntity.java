package pl.kamann.entities.reports;

import jakarta.persistence.*;
import lombok.*;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "revenue_statistics")
public class RevenueStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private MembershipCardType membershipType;

    @Column(nullable = false)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private long totalTransactions;
}
