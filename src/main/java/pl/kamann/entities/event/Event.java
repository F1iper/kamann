package pl.kamann.entities.event;

import jakarta.persistence.*;
import lombok.*;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean recurring;

    @Column(nullable = false)
    private int maxParticipants;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private AppUser createdBy;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private AppUser instructor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;

    @ManyToMany
    @JoinTable(name = "event_waitlist",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<AppUser> waitlist;

    @ManyToOne
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;
}
