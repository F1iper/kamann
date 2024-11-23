package pl.kamann.admin.service;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.history.model.ClientMembershipCardHistory;

public interface ClientMemberShipCardHistoryRepository extends JpaRepository<ClientMembershipCardHistory, Long> {
}
