package pl.kamann.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByInstructorAndStartTimeBetween(
            AppUser instructor,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Event> findByInstructor(AppUser instructor);

    @Query("SELECT e FROM Event e WHERE " +
            "(e.date BETWEEN :startDate AND :endDate OR :startDate IS NULL OR :endDate IS NULL) " +
            "AND (e.instructor.id = :instructorId OR :instructorId IS NULL) " +
            "AND (e.type = :eventType OR :eventType IS NULL) " +
            "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR :keyword IS NULL)")
    Page<Event> findFilteredEvents(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("instructorId") Long instructorId,
            @Param("eventType") String eventType,
            @Param("keyword") String keyword,
            Pageable pageable);
}