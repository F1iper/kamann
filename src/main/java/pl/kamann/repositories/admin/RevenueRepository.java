package pl.kamann.repositories.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.kamann.dtos.RevenueStat;

@Repository
public interface RevenueRepository {

    @Query("""
                SELECT new pl.kamann.dtos.RevenueStat(
                    m.type,
                    SUM(m.paymentAmount),
                    COUNT(m)
                )
                FROM MembershipPayment m
                GROUP BY m.type
            """)
    Page<RevenueStat> findRevenueStats(Pageable pageable);
}