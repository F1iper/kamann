package pl.kamann.entities.attendance;

import jakarta.persistence.*;
import lombok.*;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "occurrence_event_id", nullable = false)
    private OccurrenceEvent occurrenceEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void setDefaultTimestamp() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
