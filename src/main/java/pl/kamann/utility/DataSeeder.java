package pl.kamann.utility;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.*;
import pl.kamann.services.EventValidationService;
import pl.kamann.services.OccurrenceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private AppUserRepository appUserRepository;

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
    private OccurrenceService occurrenceService;

    @Autowired
    private EventValidationService eventValidationService;


    @PostConstruct
    public void seedData() {

        // Seed roles
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        roleRepository.save(adminRole);

        Role instructorRole = new Role();
        instructorRole.setName("INSTRUCTOR");
        roleRepository.save(instructorRole);

        Role clientRole = new Role();
        clientRole.setName("CLIENT");
        roleRepository.save(clientRole);

        // Seed admin user
        AppUser admin = new AppUser();
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFirstName("Neo");
        admin.setLastName("Matrix");
        admin.setStatus(AppUserStatus.ACTIVE);
        admin.setRoles(new HashSet<>(Arrays.asList(adminRole, instructorRole)));
        appUserRepository.save(admin);

        // Seed instructor users
        AppUser instructor1 = new AppUser();
        instructor1.setEmail("instructor1@yoga.com");
        instructor1.setPassword(passwordEncoder.encode("instructor"));
        instructor1.setFirstName("Jane");
        instructor1.setLastName("Doe");
        instructor1.setStatus(AppUserStatus.ACTIVE);
        instructor1.setRoles(new HashSet<>(List.of(instructorRole)));
        appUserRepository.save(instructor1);

        AppUser instructor2 = new AppUser();
        instructor2.setEmail("instructor2@yoga.com");
        instructor2.setPassword(passwordEncoder.encode("instructor"));
        instructor2.setFirstName("John");
        instructor2.setLastName("Smith");
        instructor2.setStatus(AppUserStatus.ACTIVE);
        instructor2.setRoles(new HashSet<>(List.of(instructorRole)));
        appUserRepository.save(instructor2);

        AppUser instructor3 = new AppUser();
        instructor3.setEmail("instructor3@yoga.com");
        instructor3.setPassword(passwordEncoder.encode("instructor"));
        instructor3.setFirstName("Mary");
        instructor3.setLastName("White");
        instructor3.setStatus(AppUserStatus.ACTIVE);
        instructor3.setRoles(new HashSet<>(List.of(instructorRole)));
        appUserRepository.save(instructor3);

        AppUser instructor4 = new AppUser();
        instructor4.setEmail("instructor4@yoga.com");
        instructor4.setPassword(passwordEncoder.encode("instructor"));
        instructor4.setFirstName("Lucas");
        instructor4.setLastName("Brown");
        instructor4.setStatus(AppUserStatus.ACTIVE);
        instructor4.setRoles(new HashSet<>(List.of(instructorRole)));
        appUserRepository.save(instructor4);

        // Seed client users
        AppUser client1 = new AppUser();
        client1.setEmail("client1@client.com");
        client1.setPassword("$2a$12$JAkc4iE85VbPq/UgzmXJd.3D9a1zt4kE78AsaohQqnHzmDEm/guo6");
        client1.setFirstName("Client1");
        client1.setLastName("Test");
        client1.setStatus(AppUserStatus.ACTIVE);
        client1.setRoles(new HashSet<>(List.of(clientRole)));
        appUserRepository.save(client1);

        AppUser client2 = new AppUser();
        client2.setEmail("client2@client.com");
        client2.setPassword("$2a$12$JAkc4iE85VbPq/UgzmXJd.3D9a1zt4kE78AsaohQqnHzmDEm/guo6");
        client2.setFirstName("Client2");
        client2.setLastName("Test");
        client2.setStatus(AppUserStatus.ACTIVE);
        client2.setRoles(new HashSet<>(List.of(clientRole)));
        appUserRepository.save(client2);

        AppUser client3 = new AppUser();
        client3.setEmail("client3@client.com");
        client3.setPassword("$2a$12$JAkc4iE85VbPq/UgzmXJd.3D9a1zt4kE78AsaohQqnHzmDEm/guo6");
        client3.setFirstName("Client3");
        client3.setLastName("Test");
        client3.setStatus(AppUserStatus.ACTIVE);
        client3.setRoles(new HashSet<>(List.of(clientRole)));
        appUserRepository.save(client3);

        AppUser client4 = new AppUser();
        client4.setEmail("client4@client.com");
        client4.setPassword("$2a$12$JAkc4iE85VbPq/UgzmXJd.3D9a1zt4kE78AsaohQqnHzmDEm/guo6");
        client4.setFirstName("Client4");
        client4.setLastName("Test");
        client4.setStatus(AppUserStatus.ACTIVE);
        client4.setRoles(new HashSet<>(List.of(clientRole)));
        appUserRepository.save(client4);

        // Seed event types
        List<String> eventTypes = Arrays.asList("Yoga", "Pilates", "Meditation", "Aerobics", "Zumba");
        eventTypes.forEach(type -> {
            EventType eventType = new EventType();
            eventType.setName(type);
            eventTypeRepository.save(eventType);
        });

        EventType yogaType = eventTypeRepository.findByName("Yoga").orElseThrow();
        EventType pilatesType = eventTypeRepository.findByName("Pilates").orElseThrow();

        // 1. Add Single Event (One-Time)
        Event singleEvent = Event.builder()
                .title("Special Yoga Workshop")
                .description("One-time intensive yoga session")
                .recurring(false)
                .startDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .maxParticipants(15)
                .eventType(yogaType)
                .createdBy(admin)
                .instructor(instructor1)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(singleEvent);

        // Add single occurrence for the single event
        OccurrenceEvent singleOccurrence = OccurrenceEvent.builder()
                .event(singleEvent)
                .date(singleEvent.getStartDate())
                .startTime(singleEvent.getStartTime())
                .endTime(singleEvent.getEndTime())
                .seriesIndex(0)
                .canceled(false)
                .createdBy(singleEvent.getCreatedBy())
                .instructor(singleEvent.getInstructor())
                .build();
        occurrenceEventRepository.save(singleOccurrence);

        // 2. Add Weekly Recurring Event (Yoga Class)
        Event recurringYoga = Event.builder()
                .title("Morning Yoga Class")
                .description("Daily morning yoga sessions")
                .recurring(true)
                .rrule("FREQ=WEEKLY;BYDAY=MO,WE,FR;INTERVAL=1;COUNT=12")
                .startDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(8, 0))
                .maxParticipants(20)
                .eventType(yogaType)
                .createdBy(admin)
                .instructor(instructor2)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(recurringYoga);

        // Generate occurrences for the weekly recurring event
        List<OccurrenceEvent> yogaOccurrences = occurrenceService.generateOccurrences(recurringYoga);
        yogaOccurrences.forEach(occurrence -> {
            OccurrenceEvent savedOccurrence = OccurrenceEvent.builder()
                    .event(recurringYoga)
                    .date(occurrence.getDate())
                    .startTime(recurringYoga.getStartTime())
                    .endTime(recurringYoga.getEndTime())
                    .seriesIndex(occurrence.getSeriesIndex())
                    .canceled(false)
                    .createdBy(recurringYoga.getCreatedBy())
                    .instructor(recurringYoga.getInstructor())
                    .build();
            occurrenceEventRepository.save(savedOccurrence);
        });

        // 3. Add Monthly Recurring Event (Pilates Workshop)
        Event monthlyWorkshop = Event.builder()
                .title("Monthly Pilates Intensive")
                .description("Advanced monthly pilates workshop")
                .recurring(true)
                .rrule("FREQ=MONTHLY;BYMONTHDAY=1;COUNT=6")
                .startDate(LocalDate.now().withDayOfMonth(1).plusMonths(1))
                .startTime(LocalTime.of(17, 30))
                .endTime(LocalTime.of(19, 30))
                .maxParticipants(15)
                .eventType(pilatesType)
                .createdBy(admin)
                .instructor(instructor2)
                .status(EventStatus.SCHEDULED)
                .build();
        eventRepository.save(monthlyWorkshop);

        // Generate occurrences for the monthly recurring event
        List<OccurrenceEvent> pilatesOccurrences = occurrenceService.generateOccurrences(monthlyWorkshop);
        pilatesOccurrences.forEach(occurrence -> {
            OccurrenceEvent savedOccurrence = OccurrenceEvent.builder()
                    .event(monthlyWorkshop)
                    .date(occurrence.getDate())
                    .startTime(monthlyWorkshop.getStartTime())
                    .endTime(monthlyWorkshop.getEndTime())
                    .seriesIndex(occurrence.getSeriesIndex())
                    .canceled(false)
                    .createdBy(monthlyWorkshop.getCreatedBy())
                    .instructor(monthlyWorkshop.getInstructor())
                    .build();
            occurrenceEventRepository.save(savedOccurrence);
        });
    }
}
