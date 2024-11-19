package pl.kamann.event.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserEventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private LocalDateTime registrationDate;

    private UserEventRegistrationStatus status;
}