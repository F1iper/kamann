package pl.kamann.classschedule.model;

import jakarta.persistence.*;
import lombok.Data;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.attendance.model.Attendance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ClassSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AppUser instructor;

    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxParticipants;

    @OneToMany(mappedBy = "classSchedule")
    private List<Attendance> attendances = new ArrayList<>();

}
