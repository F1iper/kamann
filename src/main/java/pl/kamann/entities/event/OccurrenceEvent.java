package pl.kamann.entities.event;

import jakarta.persistence.*;
import lombok.*;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_occurrence_event", columnList = "event_id,date"),
        @Index(name = "idx_occurrence_date", columnList = "date")
})
public class OccurrenceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private boolean canceled;

    private int maxParticipants;

    @Column(nullable = false)
    private int seriesIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private AppUser instructor;

    @OneToMany(mappedBy = "occurrenceEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "occurrence_event_participants",
            joinColumns = @JoinColumn(name = "occurrence_event_id"),
            inverseJoinColumns = @JoinColumn(name = "app_user_id")
    )
    private List<AppUser> participants = new ArrayList<>();

    public boolean isModified() {
        return !startTime.equals(event.getStartTime()) ||
                !endTime.equals(event.getEndTime()) ||
                canceled ||
                (instructor != null && !instructor.equals(event.getInstructor()));
    }
}