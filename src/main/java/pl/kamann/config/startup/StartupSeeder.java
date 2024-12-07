package pl.kamann.config.startup;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.reports.AttendanceStatEntity;
import pl.kamann.entities.reports.EventStatEntity;
import pl.kamann.entities.reports.RevenueStatEntity;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.membershipcard.ClientMembershipCardHistory;
import pl.kamann.repositories.UserCardHistoryRepository;
import pl.kamann.repositories.UserEventHistoryRepository;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.entities.event.UserEventRegistration;
import pl.kamann.entities.event.UserEventRegistrationStatus;
import pl.kamann.repositories.UserEventRegistrationRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.EventTypeRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.admin.AttendanceStatRepository;
import pl.kamann.repositories.admin.EventStatRepository;
import pl.kamann.repositories.admin.RevenueStatRepository;

import java.math.BigDecimal;
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
    private final EventStatRepository eventStatRepository;
    private final AttendanceStatRepository attendanceStatRepository;
    private final RevenueStatRepository revenueStatRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        AppUser admin = seedAdminUser();
        List<AppUser> instructors = seedInstructors();
        List<AppUser> clients = seedClients();
        List<EventType> eventTypes = seedEventTypes();

        // Seed Events and Related Data
        List<Event> events = seedEvents(instructors, eventTypes, admin);
        List<Event> edgeCaseEvents = seedEventsWithEdgeCases(instructors, eventTypes, admin);
        List<Event> allEvents = new ArrayList<>(events);
        allEvents.addAll(edgeCaseEvents);

        // Seed Membership Cards and Related Data
        seedMembershipCards(clients);
        seedClientRequestedCards(clients);
        seedAdminCreatedCards();

        // Seed Histories
        seedUserEventHistory(allEvents, clients);
        seedUserCardHistory(clients);

        // Seed Registrations and Attendance
        seedUserEventRegistrations(allEvents, clients);
        seedAttendance(allEvents, clients);

        // Seed Reporting Data
        seedEventStats();
        seedAttendanceStats();
        seedRevenueStats();
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
                            .orElseThrow(() -> new ApiException(
                                    "ADMIN role not found",
                                    HttpStatus.NOT_FOUND,
                                    Codes.ROLE_NOT_FOUND));

                    Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                            .orElseThrow(() -> new ApiException(
                                    "INSTRUCTOR role not found",
                                    HttpStatus.NOT_FOUND,
                                    Codes.ROLE_NOT_FOUND));

                    AppUser adminUser = new AppUser();
                    adminUser.setEmail("admin@admin.com");
                    adminUser.setPassword(passwordEncoder.encode("admin"));
                    adminUser.setFirstName("Admin");
                    adminUser.setLastName("Admin");
                    adminUser.setRoles(Set.of(adminRole, instructorRole));
                    return appUserRepository.save(adminUser);
                });
    }

    private List<AppUser> seedInstructors() {
        Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new ApiException(
                        "INSTRUCTOR role not found",
                        HttpStatus.NOT_FOUND,
                        Codes.ROLE_NOT_FOUND));

        return List.of(
                createUserIfNotExists("instructor1@yoga.com", "Jane", "Doe", instructorRole),
                createUserIfNotExists("instructor2@yoga.com", "John", "Smith", instructorRole),
                createUserIfNotExists("instructor3@yoga.com", "Mary", "White", instructorRole),
                createUserIfNotExists("instructor4@yoga.com", "Lucas", "Brown", instructorRole)
        );
    }

    private List<AppUser> seedClients() {
        Role clientRole = roleRepository.findByName("CLIENT")
                .orElseThrow(() -> new ApiException(
                        "CLIENT role not found",
                        HttpStatus.NOT_FOUND,
                        Codes.ROLE_NOT_FOUND));

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

    private void seedAdminCreatedCards() {
        if (membershipCardRepository.count() == 0) {
            MembershipCard promoCard = new MembershipCard();
            promoCard.setMembershipCardType(MembershipCardType.MONTHLY_8);
            promoCard.setEntrancesLeft(MembershipCardType.MONTHLY_8.getMaxEntrances());
            promoCard.setStartDate(LocalDateTime.now());
            promoCard.setEndDate(LocalDateTime.now().plusDays(MembershipCardType.MONTHLY_8.getValidDays()));
            promoCard.setPrice(new BigDecimal("40.00"));
            promoCard.setPaid(false);
            promoCard.setActive(false);
            promoCard.setPurchaseDate(LocalDateTime.now());

            AppUser adminUser = appUserRepository.findByEmail("admin@admin.com")
                    .orElseThrow(() -> new ApiException(
                            "Admin user not found",
                            HttpStatus.NOT_FOUND,
                            Codes.USER_NOT_FOUND));
            promoCard.setUser(adminUser);

            membershipCardRepository.save(promoCard);
        }
    }

    private void seedMembershipCards(List<AppUser> clients) {
        for (AppUser client : clients) {
            MembershipCard card = new MembershipCard();
            card.setUser(client);
            card.setMembershipCardType(MembershipCardType.MONTHLY_8);
            card.setEntrancesLeft(MembershipCardType.MONTHLY_8.getMaxEntrances());
            card.setStartDate(LocalDateTime.now());
            card.setEndDate(LocalDateTime.now().plusMonths(1));
            card.setPurchaseDate(LocalDateTime.now());
            card.setPrice(new BigDecimal("49.99"));
            card.setPaid(true);
            card.setActive(true);
            membershipCardRepository.save(card);
        }
    }

    private void seedClientRequestedCards(List<AppUser> clients) {
        for (AppUser client : clients) {
            MembershipCard card = new MembershipCard();
            card.setUser(client);
            card.setMembershipCardType(MembershipCardType.MONTHLY_8);
            card.setEntrancesLeft(MembershipCardType.MONTHLY_8.getMaxEntrances());
            card.setStartDate(LocalDateTime.now());
            card.setEndDate(LocalDateTime.now().plusDays(MembershipCardType.MONTHLY_8.getValidDays()));
            card.setPrice(new BigDecimal("50.00"));
            card.setPaid(true);
            card.setActive(true);
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
                new ClientEventHistory(null, clients.get(0), events.get(0), AttendanceStatus.PRESENT,
                        LocalDateTime.now().minusDays(1), 1, LocalDateTime.now(), LocalDateTime.now()),
                new ClientEventHistory(null, clients.get(1), events.get(1), AttendanceStatus.LATE_CANCEL,
                        LocalDateTime.now().minusDays(2), 0, LocalDateTime.now(), LocalDateTime.now()),
                new ClientEventHistory(null, clients.get(2), events.get(2), AttendanceStatus.ABSENT,
                        LocalDateTime.now().minusDays(3), 0, LocalDateTime.now(), LocalDateTime.now()),
                new ClientEventHistory(null, clients.get(3), events.get(3), AttendanceStatus.PRESENT,
                        LocalDateTime.now().minusDays(4), 1, LocalDateTime.now(), LocalDateTime.now())
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


    private void seedEventStats() {
        if (eventStatRepository.count() == 0) {
            eventStatRepository.saveAll(List.of(
                    EventStatEntity.builder()
                            .eventType("Yoga")
                            .totalEvents(50)
                            .completedEvents(45)
                            .cancelledEvents(5)
                            .build(),
                    EventStatEntity.builder()
                            .eventType("Dance")
                            .totalEvents(30)
                            .completedEvents(28)
                            .cancelledEvents(2)
                            .build(),
                    EventStatEntity.builder()
                            .eventType("Pilates")
                            .totalEvents(20)
                            .completedEvents(18)
                            .cancelledEvents(2)
                            .build(),
                    EventStatEntity.builder()
                            .eventType("CrossFit")
                            .totalEvents(25)
                            .completedEvents(20)
                            .cancelledEvents(5)
                            .build(),
                    EventStatEntity.builder()
                            .eventType("Meditation")
                            .totalEvents(15)
                            .completedEvents(15)
                            .cancelledEvents(0)
                            .build()
            ));
        }
    }

    private void seedAttendanceStats() {
        if (attendanceStatRepository.count() == 0) {
            attendanceStatRepository.saveAll(List.of(
                    AttendanceStatEntity.builder()
                            .eventName("Yoga Morning Class")
                            .totalParticipants(40)
                            .attended(35)
                            .absent(3)
                            .lateCancellations(2)
                            .build(),
                    AttendanceStatEntity.builder()
                            .eventName("Evening Dance Session")
                            .totalParticipants(25)
                            .attended(22)
                            .absent(2)
                            .lateCancellations(1)
                            .build(),
                    AttendanceStatEntity.builder()
                            .eventName("Pilates Beginners")
                            .totalParticipants(18)
                            .attended(15)
                            .absent(2)
                            .lateCancellations(1)
                            .build(),
                    AttendanceStatEntity.builder()
                            .eventName("CrossFit Extreme")
                            .totalParticipants(20)
                            .attended(18)
                            .absent(1)
                            .lateCancellations(1)
                            .build(),
                    AttendanceStatEntity.builder()
                            .eventName("Mindful Meditation")
                            .totalParticipants(15)
                            .attended(14)
                            .absent(1)
                            .lateCancellations(0)
                            .build()
            ));
        }
    }

    private void seedRevenueStats() {
        if (revenueStatRepository.count() == 0) {
            revenueStatRepository.saveAll(List.of(
                    RevenueStatEntity.builder()
                            .membershipType(MembershipCardType.MONTHLY_4)
                            .totalRevenue(BigDecimal.valueOf(5000))
                            .totalTransactions(100)
                            .build(),
                    RevenueStatEntity.builder()
                            .membershipType(MembershipCardType.MONTHLY_4)
                            .totalRevenue(BigDecimal.valueOf(2000))
                            .totalTransactions(50)
                            .build(),
                    RevenueStatEntity.builder()
                            .membershipType(MembershipCardType.SINGLE_ENTRY)
                            .totalRevenue(BigDecimal.valueOf(1000))
                            .totalTransactions(20)
                            .build()
            ));
        }
    }

}
