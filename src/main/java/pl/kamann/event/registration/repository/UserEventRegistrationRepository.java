package pl.kamann.event.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.event.model.Event;
import pl.kamann.event.registration.model.UserEventRegistration;
import pl.kamann.user.model.AppUser;

import java.util.Optional;

public interface UserEventRegistrationRepository extends JpaRepository<UserEventRegistration, Long> {

    boolean existsByUserAndEvent(AppUser user, Event event);

    Optional<UserEventRegistration> findByUserAndEvent_Id(AppUser user, Long eventId);


    long countByEvent(Event event);
}