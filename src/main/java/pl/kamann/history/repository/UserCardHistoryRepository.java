package pl.kamann.history.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.history.model.ClientMembershipCardHistory;
import pl.kamann.user.model.AppUser;

import java.util.List;

public interface UserCardHistoryRepository extends JpaRepository<ClientMembershipCardHistory, Long> {
    List<ClientMembershipCardHistory> findByUser(AppUser user);
}
