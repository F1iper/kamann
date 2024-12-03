package pl.kamann.entities.reports;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "event_statistics")
public class EventStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private long totalEvents;

    @Column(nullable = false)
    private long completedEvents;

    @Column(nullable = false)
    private long cancelledEvents;
}
