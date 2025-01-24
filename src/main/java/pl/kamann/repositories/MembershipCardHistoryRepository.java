package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.membershipcard.MembershipCardHistory;

public interface MembershipCardHistoryRepository extends JpaRepository<MembershipCardHistory, Long> {
}