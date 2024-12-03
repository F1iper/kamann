package pl.kamann.repositories.client;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.membershipcard.MembershipCardHistory;

public interface ClientMembershipCardHistoryRepository extends JpaRepository<MembershipCardHistory, Long> {
}

