package pl.kamann.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.card.model.MembershipCard;
import pl.kamann.user.model.AppUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> {
    List<MembershipCard> findByValidUntilBetween(LocalDate startDate, LocalDate endDate);

    List<MembershipCard> findByEntrancesLeftGreaterThanAndIsActiveTrue(int entrancesLeft);

    Optional<MembershipCard> findActiveCardByUser(AppUser user);
}