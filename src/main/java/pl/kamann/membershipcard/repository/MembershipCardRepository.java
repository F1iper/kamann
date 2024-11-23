package pl.kamann.membershipcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.user.model.AppUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> {

    @Query("SELECT m FROM MembershipCard m WHERE m.endDate BETWEEN :startDate AND :endDate")
    List<MembershipCard> findMembershipCardsWithinDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<MembershipCard> findByUserIdOrderByPurchaseDateDesc(Long userId);

    Optional<MembershipCard> findByUserId(Long userId);

    Optional<MembershipCard> findActiveCardByUserId(Long userId);

    List<MembershipCard> findByUser(AppUser user);
}