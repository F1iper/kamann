package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.Event;
import pl.kamann.entities.UserEventRegistration;
import pl.kamann.entities.UserEventRegistrationStatus;
import pl.kamann.entities.AppUser;

import java.util.List;
import java.util.Optional;

public interface UserEventRegistrationRepository extends JpaRepository<UserEventRegistration, Long> {

    Optional<UserEventRegistration> findByUserAndEvent(AppUser user, Event event);

    boolean existsByUserAndEvent(AppUser user, Event event);

    int countByEventAndStatus(Event event, UserEventRegistrationStatus status);

    int countByEventAndWaitlistPositionIsNotNull(Event event);

    UserEventRegistration findFirstByEventAndStatusOrderByWaitlistPositionAsc(Event event, UserEventRegistrationStatus status);

    List<UserEventRegistration> findAllByEventAndStatusOrderByWaitlistPositionAsc(Event event, UserEventRegistrationStatus status);

    Optional<UserEventRegistration> findByUserAndEventId(AppUser user, Long eventId);
}