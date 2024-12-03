package pl.kamann.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamann.entities.reports.RevenueStatEntity;

@Repository
public interface RevenueStatRepository extends JpaRepository<RevenueStatEntity, Long> {
}