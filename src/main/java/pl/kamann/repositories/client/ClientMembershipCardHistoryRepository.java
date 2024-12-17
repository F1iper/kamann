package pl.kamann.repositories.client;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.membershipcard.ClientMembershipCardHistory;

public interface ClientMembershipCardHistoryRepository extends JpaRepository<ClientMembershipCardHistory, Long> {
}

