package pl.kamann.entities.reports;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "attendance_statistics")
public class AttendanceStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private long totalParticipants;

    @Column(nullable = false)
    private long attended;

    @Column(nullable = false)
    private long absent;

    @Column(nullable = false)
    private long lateCancellations;
}
