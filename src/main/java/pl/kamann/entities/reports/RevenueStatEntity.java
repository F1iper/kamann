package pl.kamann.entities.reports;

import jakarta.persistence.*;
import lombok.*;

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
    private String membershipType;

    @Column(nullable = false)
    private double totalRevenue;

    @Column(nullable = false)
    private long totalTransactions;
}
