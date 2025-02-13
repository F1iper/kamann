package pl.kamann.entities.event;

import jakarta.persistence.*;
import lombok.*;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pl.kamann.entities.appuser.AppUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private String description;

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 1000)
    private String rrule;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;

    private String eventTypeName;

    private int maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private AppUser instructor;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Transient
    private RecurrenceRule recurrenceRule;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<OccurrenceEvent> occurrences = new ArrayList<>();

    @PostLoad
    @PostPersist
    @PostUpdate
    private void initializeRecurrenceRule() {
        if (rrule != null && !rrule.isEmpty()) {
            try {
                this.recurrenceRule = new RecurrenceRule(rrule);
            } catch (InvalidRecurrenceRuleException e) {
                throw new IllegalArgumentException("Invalid recurrence rule: " + rrule, e);
            }
        }
    }
}