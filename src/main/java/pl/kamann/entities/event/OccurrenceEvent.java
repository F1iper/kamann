package pl.kamann.entities.event;

import jakarta.persistence.*;
import lombok.*;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_occurrence_event", columnList = "event_id,start"),
        @Index(name = "idx_occurrence_start", columnList = "start")
})
public class OccurrenceEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private Integer durationMinutes;

    private boolean canceled;

    private boolean excluded;

    private AppUser createdBy;

    private int maxParticipants;

    @Column(nullable = false)
    private int seriesIndex;

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

    public LocalDateTime getEnd() {
        return start.plusMinutes(durationMinutes);
    }

    public boolean isModified() {
        return !start.equals(event.getStart()) ||
                !durationMinutes.equals(event.getDurationMinutes()) ||
                canceled ||
                excluded ||
                (instructor != null && !instructor.equals(event.getInstructor()));
    }

    @PrePersist
    @PreUpdate
    private void setDefaults() {
        if (durationMinutes == null) {
            durationMinutes = event.getDurationMinutes();
        }
        if (maxParticipants == 0) {
            maxParticipants = event.getMaxParticipants();
        }
        if (instructor == null) {
            instructor = event.getInstructor();
        }
    }
}
