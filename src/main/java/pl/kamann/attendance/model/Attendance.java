package pl.kamann.attendance.model;

import jakarta.persistence.*;
import lombok.Data;
import pl.kamann.appuser.model.AppUser;
import pl.kamann.classschedule.model.ClassSchedule;

@Entity
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ClassSchedule classSchedule;

    @ManyToOne
    private AppUser participant;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;
}
