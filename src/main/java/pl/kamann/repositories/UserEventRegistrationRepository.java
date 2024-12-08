package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.UserEventRegistration;
import pl.kamann.entities.event.UserEventRegistrationStatus;

import java.util.Optional;

public interface UserEventRegistrationRepository extends JpaRepository<UserEventRegistration, Long> {

    Optional<UserEventRegistration> findByUserAndEvent(AppUser user, Event event);

    boolean existsByUserAndEvent(AppUser user, Event event);

    int countByEventAndStatus(Event event, UserEventRegistrationStatus status);

    int countByEventAndWaitlistPositionIsNotNull(Event event);

    UserEventRegistration findFirstByEventAndStatusOrderByWaitlistPositionAsc(Event event, UserEventRegistrationStatus status);
}