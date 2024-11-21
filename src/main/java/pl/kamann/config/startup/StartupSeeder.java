package pl.kamann.config.startup;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.config.exception.specific.RoleNotFoundException;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.event.repository.EventTypeRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class StartupSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventTypeRepository eventTypeRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        AppUser admin = seedAdminUser();
        List<AppUser> instructors = seedInstructors();
        List<AppUser> clients = seedClients();
        List<EventType> eventTypes = seedEventTypes();
        List<Event> events = seedEvents(instructors, eventTypes, admin);
        seedAttendance(events, clients);
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ADMIN"));
            roleRepository.save(new Role("INSTRUCTOR"));
            roleRepository.save(new Role("USER"));
        }
    }

    private AppUser seedAdminUser() {
        if (appUserRepository.findByEmail("admin@admin.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RoleNotFoundException("ADMIN role not found in the system"));

            AppUser adminUser = new AppUser();
            adminUser.setEmail("admin@admin.com");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Adminek");
            adminUser.setRoles(Set.of(adminRole));

            return appUserRepository.save(adminUser);
        }

        return appUserRepository.findByEmail("admin@admin.com").orElseThrow();
    }

    private List<AppUser> seedInstructors() {
        Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new RoleNotFoundException("INSTRUCTOR role not found in the system"));

        AppUser instructor1 = appUserRepository.findByEmail("instructor1@yoga.com")
                .orElseGet(() -> appUserRepository.save(createInstructor("instructor1@yoga.com", "Jane", "Doe", instructorRole)));

        AppUser instructor2 = appUserRepository.findByEmail("instructor2@yoga.com")
                .orElseGet(() -> appUserRepository.save(createInstructor("instructor2@yoga.com", "John", "Smith", instructorRole)));

        return List.of(instructor1, instructor2);
    }

    private AppUser createInstructor(String email, String firstName, String lastName, Role instructorRole) {
        AppUser instructor = new AppUser();
        instructor.setEmail(email);
        instructor.setPassword(passwordEncoder.encode("password"));
        instructor.setFirstName(firstName);
        instructor.setLastName(lastName);
        instructor.setRoles(Set.of(instructorRole));
        return instructor;
    }

    private List<AppUser> seedClients() {
        Role clientRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("USER role not found in the system"));

        AppUser client1 = appUserRepository.findByEmail("client1@client.com")
                .orElseGet(() -> appUserRepository.save(createClient("client1@client.com", "Alice", "Johnson", clientRole)));

        AppUser client2 = appUserRepository.findByEmail("client2@client.com")
                .orElseGet(() -> appUserRepository.save(createClient("client2@client.com", "Bob", "Brown", clientRole)));

        return List.of(client1, client2);
    }

    private AppUser createClient(String email, String firstName, String lastName, Role clientRole) {
        AppUser client = new AppUser();
        client.setEmail(email);
        client.setPassword(passwordEncoder.encode("password"));
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setRoles(Set.of(clientRole));
        return client;
    }

    private List<EventType> seedEventTypes() {
        EventType yoga = eventTypeRepository.findByName("Yoga")
                .orElseGet(() -> eventTypeRepository.save(
                        new EventType(null, "Yoga", "A calming and relaxing physical activity for mind and body.")
                ));

        EventType dance = eventTypeRepository.findByName("Dance")
                .orElseGet(() -> eventTypeRepository.save(
                        new EventType(null, "Dance", "An expressive physical activity involving rhythmic movement.")
                ));

        return List.of(yoga, dance);
    }

    private List<Event> seedEvents(List<AppUser> instructors, List<EventType> eventTypes, AppUser createdBy) {
        Event beginnerYoga = createEvent(
                "Beginner Yoga Session",
                "A calming yoga session for beginners.",
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(11),
                instructors.get(0),
                eventTypes.get(0),
                createdBy
        );

        Event advancedDance = createEvent(
                "Advanced Dance Class",
                "An intensive dance class for experienced dancers.",
                LocalDateTime.now().plusDays(2).withHour(14),
                LocalDateTime.now().plusDays(2).withHour(15),
                instructors.get(1),
                eventTypes.get(1),
                createdBy
        );

        return eventRepository.saveAll(List.of(beginnerYoga, advancedDance));
    }

    private Event createEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime,
                              AppUser instructor, EventType eventType, AppUser createdBy) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setInstructor(instructor);
        event.setEventType(eventType);
        event.setCreatedBy(createdBy);
        event.setStatus(EventStatus.UPCOMING);
        event.setMaxParticipants(20);
        event.setRecurring(false);
        return event;
    }

    private void seedAttendance(List<Event> events, List<AppUser> clients) {
        if (clients.isEmpty() || events.isEmpty()) {
            System.out.println("No clients or events available for attendance seeding.");
            return;
        }

        Event event1 = events.get(0);
        Event event2 = events.size() > 1 ? events.get(1) : events.get(0);

        Attendance attendance1 = new Attendance();
        attendance1.setUser(clients.get(0));
        attendance1.setEvent(event1);
        attendance1.setStatus(AttendanceStatus.PRESENT);
        attendance1.setTimestamp(LocalDateTime.now());

        Attendance attendance2 = new Attendance();
        attendance2.setUser(clients.get(1));
        attendance2.setEvent(event2);
        attendance2.setStatus(AttendanceStatus.LATE_CANCEL);
        attendance2.setTimestamp(LocalDateTime.now().minusDays(1));

        attendanceRepository.saveAll(List.of(attendance1, attendance2));
    }
}