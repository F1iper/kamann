package pl.kamann.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.reports.RevenueStatEntity;

public interface RevenueStatRepository extends JpaRepository<RevenueStatEntity, Long> {
}