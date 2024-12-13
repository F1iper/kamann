package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.event.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserEventHistoryRepository extends JpaRepository<ClientEventHistory, Long> {

    List<ClientEventHistory> findByUser(AppUser user);

    Optional<ClientEventHistory> findByUserAndEvent(AppUser user, Event event);


    @Query("SELECT h FROM ClientEventHistory h WHERE h.user = :user AND " +
            "(h.attendedDate >= :startDate OR :startDate IS NULL) AND " +
            "(h.attendedDate <= :endDate OR :endDate IS NULL)")
    List<ClientEventHistory> findByUserAndDateRange(@Param("user") AppUser user,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM ClientEventHistory h WHERE h.user = :user AND h.status = :status")
    List<ClientEventHistory> findByUserAndStatus(@Param("user") AppUser user,
                                                 @Param("status") AttendanceStatus status);
}
