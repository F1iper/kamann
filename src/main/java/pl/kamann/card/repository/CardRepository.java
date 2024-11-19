package pl.kamann.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.card.model.Card;
import pl.kamann.user.model.AppUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByValidUntilBetween(LocalDate startDate, LocalDate endDate);

    List<Card> findByEntrancesLeftGreaterThanAndIsActiveTrue(int entrancesLeft);

    Optional<Card> findActiveCardByUser(AppUser user);
}