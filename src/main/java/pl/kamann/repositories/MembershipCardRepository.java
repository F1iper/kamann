package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.MembershipCard;
import pl.kamann.entities.AppUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> {

    @Query("SELECT m FROM MembershipCard m WHERE m.user.id = :userId AND m.active = true AND m.paid = true")
    Optional<MembershipCard> findActiveCardByUserId(@Param("userId") Long userId);

    Optional<MembershipCard> findById(Long id);

    Optional<MembershipCard> findByIdAndUser(Long id, AppUser user);

    boolean existsByUserAndPendingApproval(AppUser user, boolean pendingApproval);

    List<MembershipCard> findAllByUser(AppUser user);

    @Query("SELECT m FROM MembershipCard m WHERE m.endDate BETWEEN :startDate AND :endDate")
    List<MembershipCard> findMembershipCardsWithinDates(LocalDate startDate, LocalDate endDate);
}