package pl.kamann.utility;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.AuthUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.*;
import pl.kamann.services.EventValidationService;
import pl.kamann.services.admin.AdminEventService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DataSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityLookupService lookupService;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MembershipCardRepository membershipCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OccurrenceEventRepository occurrenceEventRepository;

    @Autowired
    private EventValidationService eventCreateValidationService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AdminEventService adminEventService;

    Role adminRole = new Role("ADMIN");
    Role instructorRole = new Role("INSTRUCTOR");
    Role clientRole = new Role("CLIENT");

    AppUser admin;
    AuthUser authAdmin;

    AppUser client;
    AuthUser authClient;

    @PostConstruct
    public void seedData() {
        createRoles();
        createUsers();
        seedEventTypes();
        seedEvents();
        seedAttendancesTransactional();
    }

    private void createRoles() {
        roleRepository.saveAll(Arrays.asList(adminRole, instructorRole, clientRole));
    }

    private void createUsers() {
        createDefaultAdminAndClient();
        createInstructors();
        createClients();
    }

    private void createDefaultAdminAndClient() {
        admin = AppUser.builder()
                .firstName("Admin")
                .lastName("Admin")
                .build();
        appUserRepository.save(admin);

        authAdmin = AuthUser.builder()
                .email("admin@yoga.com")
                .password(passwordEncoder.encode("admin"))
                .status(AuthUserStatus.ACTIVE)
                .appUser(admin)
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();
        authUserRepository.save(authAdmin);

        client = AppUser.builder()
                .firstName("John")
                .lastName("Wick")
                .build();
        appUserRepository.save(client);

        authClient = AuthUser.builder()
                .email("client1@client.com")
                .password(passwordEncoder.encode("admin"))
                .status(AuthUserStatus.ACTIVE)
                .appUser(client)
                .enabled(true)
                .roles(Set.of(clientRole))
                .build();
        authUserRepository.save(authClient);

    }

    private void createInstructors() {
        List<AppUser> instructors = Arrays.asList(
                createInstructor("instructor1@yoga.com", "Jane", "Doe", instructorRole),
                createInstructor("instructor2@yoga.com", "John", "Smith", instructorRole),
                createInstructor("instructor3@yoga.com", "Mary", "White", instructorRole),
                createInstructor("instructor4@yoga.com", "Lucas", "Brown", instructorRole)
        );

        appUserRepository.saveAll(instructors);
    }

    @Transactional
    private void createClients() {
        List<AppUser> clients = IntStream.range(2, 5)
                .mapToObj(i -> {
                    AuthUser authUser = AuthUser.builder()
                            .email("client" + i + "@client.com")
                            .password(passwordEncoder.encode("admin"))
                            .status(AuthUserStatus.ACTIVE)
                            .enabled(true)
                            .roles(new HashSet<>(Collections.singletonList(clientRole)))
                            .build();

                    AppUser appUser = AppUser.builder()
                            .firstName("Client" + i)
                            .lastName("Test")
                            .authUser(authUser)
                            .build();

                    authUser.setAppUser(appUser);

                    return appUser;
                })
                .collect(Collectors.toList());

        appUserRepository.saveAll(clients);
    }

    @Transactional
    private AppUser createInstructor(String email, String firstName, String lastName, Role role) {
        AuthUser authUser = AuthUser.builder()
                .email(email)
                .password(passwordEncoder.encode("admin"))
                .status(AuthUserStatus.ACTIVE)
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        AppUser appUser = AppUser.builder()
                .firstName(firstName)
                .lastName(lastName)
                .authUser(authUser)
                .build();

        authUser.setAppUser(appUser);

        return appUserRepository.save(appUser);
    }

    private void seedEventTypes() {
        List<EventType> eventTypes = new ArrayList<>();
        eventTypes.add(new EventType(1L, "Yoga", "Morning yoga"));
        eventTypes.add(new EventType(2L, "Dance", "Morning dance"));
        eventTypes.add(new EventType(3L, "PoleDance", "Morning Pole Dance"));

        eventTypes.forEach(eventType -> eventTypeRepository.save(eventType));
    }

    private void seedEvents() {
        AppUser admin = lookupService.findUserByEmail("admin@yoga.com");
        AppUser instructor = lookupService.findUserByEmail("instructor1@yoga.com");

        // Fetch EventTypes
        EventType yogaType = eventTypeRepository.findByName("Yoga")
                .orElseThrow(() -> new RuntimeException("Yoga event type not found"));
        EventType danceType = eventTypeRepository.findByName("Dance")
                .orElseThrow(() -> new RuntimeException("Dance event type not found"));
        EventType poleDanceType = eventTypeRepository.findByName("PoleDance")
                .orElseThrow(() -> new RuntimeException("PoleDance event type not found"));

        // Create single events
        createSingleYogaWorkshop(admin, instructor, yogaType);
        createSingleDanceWorkshop(admin, instructor, danceType);

        // Create single past events
        createSingleMorningTango(admin, instructor, danceType);
        createSingleEveningYoga(admin, instructor, yogaType);
        createSinglePolDanceWorkshop(admin, instructor, poleDanceType);

        // Create recurring events
        createRecurringMorningYoga(admin, instructor, yogaType);
        createRecurringPoleDance(admin, instructor, poleDanceType);
    }

    //Single future events
    private void createSingleYogaWorkshop(AppUser admin, AppUser instructor, EventType yogaType) {
        Event event = Event.builder()
                .title("Yoga Workshop")
                .description("Intensive yoga session")
                .start(LocalDateTime.now().plusDays(1)) // 1 day in the future
                .durationMinutes(120)
                .maxParticipants(15)
                .eventType(yogaType)
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    private void createSingleDanceWorkshop(AppUser admin, AppUser instructor, EventType danceType) {
        Event event = Event.builder()
                .title("Dance Workshop")
                .description("Intensive dance session")
                .start(LocalDateTime.now().plusDays(1).withHour(17).withMinute(0))
                .durationMinutes(90)
                .maxParticipants(20)
                .eventType(danceType)
                .eventTypeName(danceType.getName())
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    //Single past events
    private void createSingleMorningTango(AppUser admin, AppUser instructor, EventType danceType) {
        Event event = Event.builder()
                .title("Morning Tango")
                .description("Relaxing Tango session")
                .start(LocalDateTime.now().minusDays(8).withHour(19).withMinute(0))
                .durationMinutes(90)
                .maxParticipants(25)
                .eventType(danceType)
                .eventTypeName(danceType.getName())
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.COMPLETED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    private void createSinglePolDanceWorkshop(AppUser admin, AppUser instructor, EventType danceType) {
        Event event = Event.builder()
                .title("Pole Dance Workshop")
                .description("Try this")
                .start(LocalDateTime.now().minusDays(10).withHour(17).withMinute(0))
                .durationMinutes(100)
                .maxParticipants(30)
                .eventType(danceType)
                .eventTypeName(danceType.getName())
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.COMPLETED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    private void createSingleEveningYoga(AppUser admin, AppUser instructor, EventType danceType) {
        Event event = Event.builder()
                .title("Evening Yoga")
                .description("Relaxing yoga session")
                .start(LocalDateTime.now().minusDays(6).withHour(16).withMinute(0))
                .durationMinutes(100)
                .maxParticipants(30)
                .eventType(danceType)
                .eventTypeName(danceType.getName())
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.COMPLETED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    //Recurring future events
    private void createRecurringMorningYoga(AppUser admin, AppUser instructor, EventType yogaType) {
        Event event = Event.builder()
                .title("Morning Yoga")
                .description("Daily morning yoga sessions")
                .rrule("FREQ=WEEKLY;BYDAY=MO,WE,FR;INTERVAL=1;COUNT=12")
                .start(LocalDateTime.now().plusDays(2).withHour(7).withMinute(0))
                .durationMinutes(60)
                .maxParticipants(20)
                .eventType(yogaType)
                .eventTypeName(yogaType.getName())
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    private void createRecurringPoleDance(AppUser admin, AppUser instructor, EventType poleDanceType) {
        Event event = Event.builder()
                .title("Evening Pole Dance")
                .description("Weekly pole dance classes")
                .rrule("FREQ=WEEKLY;BYDAY=TU,TH;INTERVAL=1;COUNT=10")
                .start(LocalDateTime.now().plusDays(3).withHour(19).withMinute(0))
                .durationMinutes(75)
                .maxParticipants(12)
                .eventType(poleDanceType)
                .eventTypeName(poleDanceType.getName())
                .createdBy(admin)
                .instructor(instructor)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(event);
        List<OccurrenceEvent> occurrenceEvents = adminEventService.generateOccurrences(event);
        occurrenceEvents.forEach(occurrenceEvent ->
                occurrenceEventRepository.save(occurrenceEvent)
        );
    }

    @Transactional
    public void seedAttendancesTransactional() {
        seedAttendances();
    }

    private void seedAttendances() {


        // For each event, enroll client1 in the first occurrence (if available)
        seedAttendanceForEvent("Yoga Workshop");
        seedAttendanceForEvent("Morning Yoga");
        seedAttendanceForEvent("Dance Workshop");
        seedAttendanceForEvent("Evening Pole Dance");
        seedAttendanceForEvent("Evening Yoga");
        seedAttendanceForEvent("Pole Dance Workshop");
        seedAttendanceForEvent("Morning Tango");
    }

    private void seedAttendanceForEvent(String eventTitle) {
        List<Attendance> attendances = attendanceRepository.findAll();
        Optional<Event> eventOpt = eventRepository.findByTitle(eventTitle);
        eventOpt.ifPresent(event -> {
            var occurrences = occurrenceEventRepository.findOccurrencesByEventId(event.getId());
            if (!occurrences.isEmpty()) {
                OccurrenceEvent occurrence = occurrences.get(0);
                // Force initialization of the lazy collection
                occurrence.getParticipants().size();
                if (!occurrence.getParticipants().contains(client)) {
                    Attendance attendance = Attendance.builder()
                            .user(client)
                            .occurrenceEvent(occurrence)
                            .status(AttendanceStatus.REGISTERED)
                            .build();
                    occurrence.getParticipants().add(client);
                    occurrenceEventRepository.save(occurrence);
                    attendanceRepository.save(attendance);
                }
            }
        });
    }
}