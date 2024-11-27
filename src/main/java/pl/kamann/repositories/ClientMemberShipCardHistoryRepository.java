package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.ClientMembershipCardHistory;

public interface ClientMemberShipCardHistoryRepository extends JpaRepository<ClientMembershipCardHistory, Long> {
}
