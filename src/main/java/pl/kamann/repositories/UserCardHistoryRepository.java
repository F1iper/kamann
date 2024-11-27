package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.ClientMembershipCardHistory;
import pl.kamann.entities.AppUser;

import java.util.List;

public interface UserCardHistoryRepository extends JpaRepository<ClientMembershipCardHistory, Long> {
    List<ClientMembershipCardHistory> findByUser(AppUser user);
}
