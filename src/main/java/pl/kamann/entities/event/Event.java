package pl.kamann.entities.event;

import jakarta.persistence.*;
import lombok.*;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import pl.kamann.entities.appuser.AppUser;

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
@Table(indexes = @Index(name = "idx_recurring", columnList = "recurring"))
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Boolean recurring;

    private String rrule;

    @ElementCollection
    @CollectionTable(name = "event_exdates", joinColumns = @JoinColumn(name = "event_id"))
    private List<LocalDate> exdates = new ArrayList<>();

    private Integer occurrenceLimit;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private AppUser createdBy;

    @ManyToOne
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;

    private int maxParticipants;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private AppUser instructor;

    @Transient
    private RecurrenceRule recurrenceRule;

    @Column
    private LocalDate recurrenceEndDate;

    @PostLoad
    @PostPersist
    @PostUpdate
    private void initializeRecurrenceRule() {
        if (Boolean.TRUE.equals(recurring) && rrule != null && !rrule.isEmpty()) {
            try {
                this.recurrenceRule = new RecurrenceRule(rrule);
            } catch (InvalidRecurrenceRuleException e) {
                throw new IllegalArgumentException("Invalid recurrence rule: " + rrule, e);
            }
        }
    }
}