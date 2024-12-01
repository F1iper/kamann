package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.ClientEventHistory;

import java.time.LocalDate;
import java.util.List;

public interface UserEventHistoryRepository extends JpaRepository<ClientEventHistory, Long> {

    List<ClientEventHistory> findByUser(AppUser user);

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
