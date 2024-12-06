package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> {

    Optional<MembershipCard> findById(Long id);

    List<MembershipCard> findByEndDateBeforeAndActiveTrue(LocalDateTime dateTime);

    List<MembershipCard> findByUserIsNullAndActiveFalse();

    Optional<MembershipCard> findActiveCardByUserId(Long userId);

    List<MembershipCard> findAllByUser(AppUser user);

    List<MembershipCard> findByUserIdAndActiveTrue(Long clientId);
}