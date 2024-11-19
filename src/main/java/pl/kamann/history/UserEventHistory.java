package pl.kamann.history;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserEventHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private LocalDateTime attendedDate;
    private int entrancesUsed;
}