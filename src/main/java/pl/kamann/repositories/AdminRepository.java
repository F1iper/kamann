package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kamann.entities.ClientEventHistory;
import pl.kamann.entities.ClientMembershipCardHistory;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<ClientMembershipCardHistory, Long> {

    // Find Membership Card History by User ID
    @Query("SELECT h FROM ClientMembershipCardHistory h WHERE h.user.id = :userId")
    List<ClientMembershipCardHistory> findClientMembershipCardHistoryByUserId(@Param("userId") Long userId);

    // Find Event History by User ID
    @Query("SELECT h FROM ClientEventHistory h WHERE h.user.id = :userId")
    List<ClientEventHistory> findClientEventHistoryByUserId(@Param("userId") Long userId);

    // Find Event History by Event ID
    @Query("SELECT h FROM ClientEventHistory h WHERE h.event.id = :eventId")
    List<ClientEventHistory> findClientEventHistoryByEventId(@Param("eventId") Long eventId);
}
