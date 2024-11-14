package pl.kamann.classschedule.model;

import jakarta.persistence.*;
import pl.kamann.appuser.model.AppUser;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private AppUser instructor;

    private Integer maxParticipants;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany
    private List<AppUser> participants;
}
