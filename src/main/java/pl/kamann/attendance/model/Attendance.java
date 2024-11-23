package pl.kamann.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

@Entity
@Data
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
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @PrePersist
    public void setDefaultTimestamp() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public void setCancelledByInstructor(boolean b) {

    }
}