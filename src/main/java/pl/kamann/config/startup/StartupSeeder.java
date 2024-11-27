package pl.kamann.config.startup;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.entities.Attendance;
import pl.kamann.entities.AttendanceStatus;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.entities.Event;
import pl.kamann.entities.EventStatus;
import pl.kamann.entities.EventType;
import pl.kamann.entities.Role;
import pl.kamann.entities.ClientEventHistory;
import pl.kamann.entities.ClientMembershipCardHistory;
import pl.kamann.repositories.UserCardHistoryRepository;
import pl.kamann.repositories.UserEventHistoryRepository;
import pl.kamann.entities.MembershipCard;
import pl.kamann.entities.MembershipCardType;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.entities.UserEventRegistration;
import pl.kamann.entities.UserEventRegistrationStatus;
import pl.kamann.repositories.UserEventRegistrationRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.EventTypeRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.repositories.AppUserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final MembershipCardRepository membershipCardRepository;
    private final UserCardHistoryRepository userCardHistoryRepository;
    private final UserEventHistoryRepository userEventHistoryRepository;
    private final UserEventRegistrationRepository userEventRegistrationRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        AppUser admin = seedAdminUser();
        List<AppUser> instructors = seedInstructors();
        List<AppUser> clients = seedClients();
        List<EventType> eventTypes = seedEventTypes();

        List<Event> events = seedEvents(instructors, eventTypes, admin);
        List<Event> edgeCaseEvents = seedEventsWithEdgeCases(instructors, eventTypes, admin);
        List<Event> allEvents = new ArrayList<>(events);
        allEvents.addAll(edgeCaseEvents);

        seedMembershipCards(clients);
        seedUserEventHistory(allEvents, clients);
        seedUserCardHistory(clients);
        seedUserEventRegistrations(allEvents, clients);
        seedEventsWithEdgeCases(instructors, eventTypes, admin);
        seedAttendance(allEvents, clients);
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role("ADMIN"),
                    new Role("INSTRUCTOR"),
                    new Role("CLIENT")
            ));
        }
    }

    private AppUser seedAdminUser() {
        return appUserRepository.findByEmail("admin@admin.com")
                .orElseGet(() -> {
                    Role adminRole = roleRepository.findByName("ADMIN")
                            .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

                    AppUser adminUser = new AppUser();
                    adminUser.setEmail("admin@admin.com");
                    adminUser.setPassword(passwordEncoder.encode("admin"));
                    adminUser.setFirstName("Admin");
                    adminUser.setLastName("Admin");
                    adminUser.setRoles(Set.of(adminRole));

                    return appUserRepository.save(adminUser);
                });
    }

    private List<AppUser> seedInstructors() {
        Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new IllegalStateException("INSTRUCTOR role not found"));

        return List.of(
                createUserIfNotExists("instructor1@yoga.com", "Jane", "Doe", instructorRole),
                createUserIfNotExists("instructor2@yoga.com", "John", "Smith", instructorRole),
                createUserIfNotExists("instructor3@yoga.com", "Mary", "White", instructorRole),
                createUserIfNotExists("instructor4@yoga.com", "Lucas", "Brown", instructorRole)
        );
    }

    private List<AppUser> seedClients() {
        Role clientRole = roleRepository.findByName("CLIENT")
                .orElseThrow(() -> new IllegalStateException("CLIENT role not found"));

        return List.of(
                createUserIfNotExists("client1@client.com", "Alice", "Johnson", clientRole),
                createUserIfNotExists("client2@client.com", "Bob", "Brown", clientRole),
                createUserIfNotExists("client3@client.com", "Charlie", "Black", clientRole),
                createUserIfNotExists("client4@client.com", "Diana", "Green", clientRole),
                createUserIfNotExists("client5@client.com", "Eve", "Taylor", clientRole),
                createUserIfNotExists("client6@client.com", "Frank", "Miller", clientRole)
        );
    }

    private AppUser createUserIfNotExists(String email, String firstName, String lastName, Role role) {
        return appUserRepository.findByEmail(email)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("password"));
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRoles(Set.of(role));
                    return appUserRepository.save(user);
                });
    }

    private List<EventType> seedEventTypes() {
        return List.of(
                eventTypeRepository.findByName("Yoga").orElseGet(() -> eventTypeRepository.save(new EventType(null, "Yoga", "Relaxing yoga session."))),
                eventTypeRepository.findByName("Dance").orElseGet(() -> eventTypeRepository.save(new EventType(null, "Dance", "Energetic dance class."))),
                eventTypeRepository.findByName("Pilates").orElseGet(() -> eventTypeRepository.save(new EventType(null, "Pilates", "Strengthening Pilates session."))),
                eventTypeRepository.findByName("CrossFit").orElseGet(() -> eventTypeRepository.save(new EventType(null, "CrossFit", "High-intensity interval training."))),
                eventTypeRepository.findByName("Meditation").orElseGet(() -> eventTypeRepository.save(new EventType(null, "Meditation", "Relaxation and mindfulness session.")))
        );
    }

    private List<Event> seedEvents(List<AppUser> instructors, List<EventType> eventTypes, AppUser createdBy) {
        return eventRepository.saveAll(List.of(
                createEvent("Morning Yoga", "Morning yoga for all levels.",
                        LocalDateTime.now().plusDays(1).withHour(7), LocalDateTime.now().plusDays(1).withHour(8),
                        instructors.get(0), eventTypes.get(0), createdBy),
                createEvent("Evening Dance", "Fun evening dance class.",
                        LocalDateTime.now().plusDays(1).withHour(18), LocalDateTime.now().plusDays(1).withHour(19),
                        instructors.get(1), eventTypes.get(1), createdBy),
                createEvent("Pilates for Beginners", "Introduction to Pilates.",
                        LocalDateTime.now().plusDays(2).withHour(10), LocalDateTime.now().plusDays(2).withHour(11),
                        instructors.get(2), eventTypes.get(2), createdBy),
                createEvent("CrossFit Extreme", "Push your limits with this CrossFit session.",
                        LocalDateTime.now().plusDays(3).withHour(6), LocalDateTime.now().plusDays(3).withHour(7),
                        instructors.get(3), eventTypes.get(3), createdBy),
                createEvent("Mindful Meditation", "Evening mindfulness session.",
                        LocalDateTime.now().plusDays(3).withHour(20), LocalDateTime.now().plusDays(3).withHour(21),
                        instructors.get(2), eventTypes.get(4), createdBy)
        ));
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

    private void seedMembershipCards(List<AppUser> clients) {
        for (AppUser client : clients) {
            MembershipCard card = new MembershipCard();
            card.setUser(client);
            card.setMembershipCardType(MembershipCardType.MONTHLY_8);
            card.setEntrancesLeft(8);
            card.setStartDate(LocalDateTime.now());
            card.setEndDate(LocalDateTime.now().plusMonths(1));
            card.setPurchaseDate(LocalDateTime.now());
            card.setPaid(true);
            membershipCardRepository.save(card);
        }
    }

    private void seedAttendance(List<Event> events, List<AppUser> clients) {
        attendanceRepository.saveAll(List.of(
                new Attendance(null, clients.get(0), events.get(0), AttendanceStatus.PRESENT, LocalDateTime.now()),
                new Attendance(null, clients.get(1), events.get(1), AttendanceStatus.LATE_CANCEL, LocalDateTime.now().minusHours(3)),
                new Attendance(null, clients.get(2), events.get(2), AttendanceStatus.EARLY_CANCEL, LocalDateTime.now().minusDays(1)),
                new Attendance(null, clients.get(3), events.get(3), AttendanceStatus.ABSENT, LocalDateTime.now())
        ));
    }

    private void seedUserEventHistory(List<Event> events, List<AppUser> clients) {
        userEventHistoryRepository.saveAll(List.of(
                new ClientEventHistory(null, clients.get(0), events.get(0), AttendanceStatus.PRESENT, LocalDateTime.now().minusDays(1), 1),
                new ClientEventHistory(null, clients.get(1), events.get(1), AttendanceStatus.LATE_CANCEL, LocalDateTime.now().minusDays(2), 0),
                new ClientEventHistory(null, clients.get(2), events.get(2), AttendanceStatus.ABSENT, LocalDateTime.now().minusDays(3), 0),
                new ClientEventHistory(null, clients.get(3), events.get(3), AttendanceStatus.PRESENT, LocalDateTime.now().minusDays(4), 1)
        ));
    }

    private void seedUserCardHistory(List<AppUser> clients) {
        if (clients == null || clients.size() < 3) {
            throw new IllegalArgumentException("At least 3 clients are required to seed the user card history.");
        }

        List<ClientMembershipCardHistory> histories = List.of(
                new ClientMembershipCardHistory(
                        null,
                        clients.get(0),
                        MembershipCardType.MONTHLY_4,
                        LocalDateTime.now().minusMonths(1),
                        LocalDateTime.now(),
                        MembershipCardType.MONTHLY_4.getMaxEntrances(),
                        2,
                        true
                ),
                new ClientMembershipCardHistory(
                        null,
                        clients.get(1),
                        MembershipCardType.MONTHLY_8,
                        LocalDateTime.now().minusMonths(2),
                        LocalDateTime.now(),
                        MembershipCardType.MONTHLY_8.getMaxEntrances(),
                        1,
                        true
                ),
                new ClientMembershipCardHistory(
                        null,
                        clients.get(2),
                        MembershipCardType.SINGLE_ENTRY,
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now(),
                        MembershipCardType.SINGLE_ENTRY.getMaxEntrances(),
                        0,
                        true
                )
        );

        userCardHistoryRepository.saveAll(histories);
    }


    private void seedUserEventRegistrations(List<Event> events, List<AppUser> clients) {
        userEventRegistrationRepository.saveAll(List.of(
                new UserEventRegistration(null, clients.get(0), events.get(0),
                        UserEventRegistrationStatus.REGISTERED, null, LocalDateTime.now()),
                new UserEventRegistration(null, clients.get(1), events.get(1),
                        UserEventRegistrationStatus.REGISTERED, null, LocalDateTime.now().minusDays(1)),
                new UserEventRegistration(null, clients.get(2), events.get(2),
                        UserEventRegistrationStatus.REGISTERED, 1, LocalDateTime.now().minusDays(2)),
                new UserEventRegistration(null, clients.get(3), events.get(3),
                        UserEventRegistrationStatus.WAITLISTED, 2, LocalDateTime.now().minusDays(3))
        ));
    }

    private List<Event> seedEventsWithEdgeCases(List<AppUser> instructors, List<EventType> eventTypes, AppUser createdBy) {
        List<Event> edgeCaseEvents = List.of(
                createEvent("Fully Booked Event", "This event is fully booked.",
                        LocalDateTime.now().plusDays(4).withHour(10), LocalDateTime.now().plusDays(4).withHour(12),
                        instructors.get(0), eventTypes.get(1), createdBy),
                createEvent("Recurring Yoga Session", "Weekly yoga session.",
                        LocalDateTime.now().plusDays(5).withHour(8), LocalDateTime.now().plusDays(5).withHour(9),
                        instructors.get(1), eventTypes.get(0), createdBy),
                createEvent("Waitlisted Meditation", "Meditation session with a waitlist.",
                        LocalDateTime.now().plusDays(6).withHour(19), LocalDateTime.now().plusDays(6).withHour(20),
                        instructors.get(2), eventTypes.get(4), createdBy)
        );

        return eventRepository.saveAll(edgeCaseEvents);
    }

}
