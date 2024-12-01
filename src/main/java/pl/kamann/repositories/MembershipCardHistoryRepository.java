package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.MembershipCard;
import pl.kamann.entities.MembershipCardHistory;

import java.util.List;

public interface MembershipCardHistoryRepository extends JpaRepository<MembershipCardHistory, Long> {
    List<MembershipCardHistory> findByUser(AppUser user);
    List<MembershipCardHistory> findByCard(MembershipCard card);
}