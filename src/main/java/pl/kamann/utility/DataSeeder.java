package pl.kamann.utility;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.event.*;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MembershipCardRepository membershipCardRepository;

    @Autowired
    private UserEventRegistrationRepository userEventRegistrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


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
        EventType yoga = new EventType();
        yoga.setName("Yoga");
        yoga.setDescription("Yoga classes for all levels.");
        eventTypeRepository.save(yoga);

        EventType dance = new EventType();
        dance.setName("Dance");
        dance.setDescription("Fun and energetic dance classes.");
        eventTypeRepository.save(dance);

        EventType pilates = new EventType();
        pilates.setName("Pilates");
        pilates.setDescription("Introduction to Pilates.");
        eventTypeRepository.save(pilates);

        EventType crossFit = new EventType();
        crossFit.setName("CrossFit");
        crossFit.setDescription("High-intensity CrossFit sessions.");
        eventTypeRepository.save(crossFit);

        EventType strengthTraining = new EventType();
        strengthTraining.setName("Strength Training");
        strengthTraining.setDescription("Build strength with guided exercises.");
        eventTypeRepository.save(strengthTraining);

        EventType stretching = new EventType();
        stretching.setName("Stretching");
        stretching.setDescription("Relaxing stretching sessions.");
        eventTypeRepository.save(stretching);

        // Seed events
        Event morningYoga = new Event();
        morningYoga.setTitle("Morning Yoga");
        morningYoga.setDescription("Yoga under the sun, experience nature.");
        morningYoga.setStartDate(LocalDate.of(2025, 4, 6));
        morningYoga.setEndDate(LocalDate.of(2025, 4, 6));
        morningYoga.setTime(LocalTime.of(8, 0));
        morningYoga.setRecurring(false);
        morningYoga.setMaxParticipants(30);
        morningYoga.setStatus(EventStatus.UPCOMING);
        morningYoga.setCreatedBy(admin);
        morningYoga.setInstructor(instructor1);
        morningYoga.setEventType(yoga);
        eventRepository.save(morningYoga);

        Event eveningDance = new Event();
        eveningDance.setTitle("Evening Dance");
        eveningDance.setDescription("Fun dance party on the weekend.");
        eveningDance.setStartDate(LocalDate.of(2025, 2, 11));
        eveningDance.setEndDate(LocalDate.of(2025, 2, 11));
        eveningDance.setTime(LocalTime.of(20, 0));
        eveningDance.setRecurring(false);
        eveningDance.setMaxParticipants(50);
        eveningDance.setStatus(EventStatus.UPCOMING);
        eveningDance.setCreatedBy(admin);
        eveningDance.setInstructor(instructor2);
        eveningDance.setEventType(dance);
        eventRepository.save(eveningDance);

        // Seed more events
        Event pilatesForBeginners = new Event();
        pilatesForBeginners.setTitle("Pilates for Beginners");
        pilatesForBeginners.setDescription("Introduction to Pilates.");
        pilatesForBeginners.setStartDate(LocalDate.of(2025, 3, 3));
        pilatesForBeginners.setEndDate(LocalDate.of(2025, 3, 3));
        pilatesForBeginners.setTime(LocalTime.of(10, 0));
        pilatesForBeginners.setRecurring(false);
        pilatesForBeginners.setMaxParticipants(20);
        pilatesForBeginners.setStatus(EventStatus.UPCOMING);
        pilatesForBeginners.setCreatedBy(admin);
        pilatesForBeginners.setInstructor(instructor3);
        pilatesForBeginners.setEventType(pilates);
        eventRepository.save(pilatesForBeginners);

        Event crossFitExtreme = new Event();
        crossFitExtreme.setTitle("CrossFit Extreme");
        crossFitExtreme.setDescription("Push your limits with this CrossFit session.");
        crossFitExtreme.setStartDate(LocalDate.of(2025, 2, 4));
        crossFitExtreme.setEndDate(LocalDate.of(2025, 2, 4));
        crossFitExtreme.setTime(LocalTime.of(6, 0));
        crossFitExtreme.setRecurring(false);
        crossFitExtreme.setMaxParticipants(20);
        crossFitExtreme.setStatus(EventStatus.UPCOMING);
        crossFitExtreme.setCreatedBy(admin);
        crossFitExtreme.setInstructor(instructor4);
        crossFitExtreme.setEventType(crossFit);
        eventRepository.save(crossFitExtreme);

        Event advancedPilates = new Event();
        advancedPilates.setTitle("Advanced Pilates");
        advancedPilates.setDescription("For those looking to challenge their Pilates practice.");
        advancedPilates.setStartDate(LocalDate.of(2025, 3, 10));
        advancedPilates.setEndDate(LocalDate.of(2025, 3, 10));
        advancedPilates.setTime(LocalTime.of(9, 0));
        advancedPilates.setRecurring(false);
        advancedPilates.setMaxParticipants(15);
        advancedPilates.setStatus(EventStatus.UPCOMING);
        advancedPilates.setCreatedBy(admin);
        advancedPilates.setInstructor(instructor3);
        advancedPilates.setEventType(pilates);
        eventRepository.save(advancedPilates);

        Event morningCrossFit = new Event();
        morningCrossFit.setTitle("Morning CrossFit");
        morningCrossFit.setDescription("Intense CrossFit to start the day.");
        morningCrossFit.setStartDate(LocalDate.of(2025, 5, 21));
        morningCrossFit.setEndDate(LocalDate.of(2025, 5, 21));
        morningCrossFit.setTime(LocalTime.of(6, 0));
        morningCrossFit.setRecurring(false);
        morningCrossFit.setMaxParticipants(25);
        morningCrossFit.setStatus(EventStatus.UPCOMING);
        morningCrossFit.setCreatedBy(admin);
        morningCrossFit.setInstructor(instructor4);
        morningCrossFit.setEventType(crossFit);
        eventRepository.save(morningCrossFit);

        Event yogaForAll = new Event();
        yogaForAll.setTitle("Yoga for All");
        yogaForAll.setDescription("Yoga for all levels, from beginner to expert.");
        yogaForAll.setStartDate(LocalDate.of(2025, 3, 1));
        yogaForAll.setEndDate(LocalDate.of(2025, 7, 1));
        yogaForAll.setTime(LocalTime.of(7, 30));
        yogaForAll.setRecurring(true);
        yogaForAll.setMaxParticipants(40);
        yogaForAll.setStatus(EventStatus.UPCOMING);
        yogaForAll.setCreatedBy(admin);
        yogaForAll.setInstructor(instructor1);
        yogaForAll.setEventType(yoga);
        yogaForAll.setFrequency(EventFrequency.WEEKLY);
        yogaForAll.setDaysOfWeek(("MONDAY, THURSDAY"));
        yogaForAll.setRecurrenceEndDate(LocalDate.of(2025, 7, 1));
        eventRepository.save(yogaForAll);

        Event stretchAndStrength = new Event();
        stretchAndStrength.setTitle("Stretch and Strength");
        stretchAndStrength.setDescription("Combining stretching and strength training.");
        stretchAndStrength.setStartDate(LocalDate.of(2025, 3, 23));
        stretchAndStrength.setEndDate(LocalDate.of(2025, 6, 23));
        stretchAndStrength.setTime(LocalTime.of(8, 30));
        stretchAndStrength.setRecurring(true);
        stretchAndStrength.setMaxParticipants(25);
        stretchAndStrength.setStatus(EventStatus.UPCOMING);
        stretchAndStrength.setCreatedBy(admin);
        stretchAndStrength.setInstructor(instructor2);
        stretchAndStrength.setEventType(strengthTraining);
        stretchAndStrength.setFrequency(EventFrequency.WEEKLY);
        stretchAndStrength.setDaysOfWeek("MONDAY, FRIDAY");
        stretchAndStrength.setRecurrenceEndDate(LocalDate.of(2025, 6, 23));
        eventRepository.save(stretchAndStrength);

        Event eveningYoga = new Event();
        eveningYoga.setTitle("Evening Yoga");
        eveningYoga.setDescription("Relaxing yoga session to end your day.");
        eveningYoga.setStartDate(LocalDate.of(2025, 4, 10));
        eveningYoga.setEndDate(LocalDate.of(2025, 4, 10));
        eveningYoga.setTime(LocalTime.of(18, 0));
        eveningYoga.setRecurring(false);
        eveningYoga.setMaxParticipants(30);
        eveningYoga.setStatus(EventStatus.UPCOMING);
        eveningYoga.setCreatedBy(admin);
        eveningYoga.setInstructor(instructor1);
        eveningYoga.setEventType(yoga);
        eventRepository.save(eveningYoga);

        Event weekendDanceParty = new Event();
        weekendDanceParty.setTitle("Weekend Dance Party");
        weekendDanceParty.setDescription("A fun dance party to celebrate the weekend.");
        weekendDanceParty.setStartDate(LocalDate.of(2025, 5, 15));
        weekendDanceParty.setEndDate(LocalDate.of(2025, 5, 15));
        weekendDanceParty.setTime(LocalTime.of(19, 0));
        weekendDanceParty.setRecurring(false);
        weekendDanceParty.setMaxParticipants(50);
        weekendDanceParty.setStatus(EventStatus.UPCOMING);
        weekendDanceParty.setCreatedBy(admin);
        weekendDanceParty.setInstructor(instructor2);
        weekendDanceParty.setEventType(dance);
        eventRepository.save(weekendDanceParty);

        Event mindfulMeditation = new Event();
        mindfulMeditation.setTitle("Mindful Meditation");
        mindfulMeditation.setDescription("A guided meditation session for relaxation.");
        mindfulMeditation.setStartDate(LocalDate.of(2025, 6, 1));
        mindfulMeditation.setEndDate(LocalDate.of(2025, 6, 1));
        mindfulMeditation.setTime(LocalTime.of(9, 0));
        mindfulMeditation.setRecurring(false);
        mindfulMeditation.setMaxParticipants(20);
        mindfulMeditation.setStatus(EventStatus.UPCOMING);
        mindfulMeditation.setCreatedBy(admin);
        mindfulMeditation.setInstructor(instructor3);
        mindfulMeditation.setEventType(stretching);
        eventRepository.save(mindfulMeditation);

        Event strengthTrainingBootcamp = new Event();
        strengthTrainingBootcamp.setTitle("Strength Training Bootcamp");
        strengthTrainingBootcamp.setDescription("Intense strength training session.");
        strengthTrainingBootcamp.setStartDate(LocalDate.of(2025, 7, 10));
        strengthTrainingBootcamp.setEndDate(LocalDate.of(2025, 7, 10));
        strengthTrainingBootcamp.setTime(LocalTime.of(7, 0));
        strengthTrainingBootcamp.setRecurring(false);
        strengthTrainingBootcamp.setMaxParticipants(25);
        strengthTrainingBootcamp.setStatus(EventStatus.UPCOMING);
        strengthTrainingBootcamp.setCreatedBy(admin);
        strengthTrainingBootcamp.setInstructor(instructor4);
        strengthTrainingBootcamp.setEventType(strengthTraining);
        eventRepository.save(strengthTrainingBootcamp);

        // Seed membership cards for admin
        MembershipCard adminSingleEntryCard = new MembershipCard();
        adminSingleEntryCard.setUser(admin);
        adminSingleEntryCard.setMembershipCardType(MembershipCardType.SINGLE_ENTRY);
        adminSingleEntryCard.setEntrancesLeft(1);
        adminSingleEntryCard.setStartDate(LocalDateTime.now());
        adminSingleEntryCard.setEndDate(LocalDateTime.now().plusMonths(1));
        adminSingleEntryCard.setPaid(false);
        adminSingleEntryCard.setActive(true);
        adminSingleEntryCard.setPendingApproval(false);
        adminSingleEntryCard.setPrice(new BigDecimal("10.00"));
        adminSingleEntryCard.setPurchaseDate(LocalDateTime.now());
        membershipCardRepository.save(adminSingleEntryCard);

        MembershipCard adminMonthly4Card = new MembershipCard();
        adminMonthly4Card.setUser(admin);
        adminMonthly4Card.setMembershipCardType(MembershipCardType.MONTHLY_4);
        adminMonthly4Card.setEntrancesLeft(4);
        adminMonthly4Card.setStartDate(LocalDateTime.now());
        adminMonthly4Card.setEndDate(LocalDateTime.now().plusMonths(1));
        adminMonthly4Card.setPaid(false);
        adminMonthly4Card.setActive(true);
        adminMonthly4Card.setPendingApproval(false);
        adminMonthly4Card.setPrice(new BigDecimal("20.00"));
        adminMonthly4Card.setPurchaseDate(LocalDateTime.now());
        membershipCardRepository.save(adminMonthly4Card);

// Seed membership cards for clients
        MembershipCard client1SingleEntryCard = new MembershipCard();
        client1SingleEntryCard.setUser(client1);
        client1SingleEntryCard.setMembershipCardType(MembershipCardType.SINGLE_ENTRY);
        client1SingleEntryCard.setEntrancesLeft(1);
        client1SingleEntryCard.setStartDate(LocalDateTime.now());
        client1SingleEntryCard.setEndDate(LocalDateTime.now().plusMonths(1));
        client1SingleEntryCard.setPaid(true);
        client1SingleEntryCard.setActive(true);
        client1SingleEntryCard.setPendingApproval(false);
        client1SingleEntryCard.setPrice(new BigDecimal("10.00"));
        client1SingleEntryCard.setPurchaseDate(LocalDateTime.now());
        membershipCardRepository.save(client1SingleEntryCard);

        MembershipCard client2Monthly4Card = new MembershipCard();
        client2Monthly4Card.setUser(client2);
        client2Monthly4Card.setMembershipCardType(MembershipCardType.SINGLE_ENTRY);
        client2Monthly4Card.setEntrancesLeft(4);
        client2Monthly4Card.setStartDate(LocalDateTime.now());
        client2Monthly4Card.setEndDate(LocalDateTime.now().plusMonths(1));
        client2Monthly4Card.setPaid(true);
        client2Monthly4Card.setActive(true);
        client2Monthly4Card.setPendingApproval(false);
        client2Monthly4Card.setPrice(new BigDecimal("20.00"));
        client2Monthly4Card.setPurchaseDate(LocalDateTime.now());
        membershipCardRepository.save(client2Monthly4Card);

        MembershipCard client3Monthly8Card = new MembershipCard();
        client3Monthly8Card.setUser(client3);
        client3Monthly8Card.setMembershipCardType(MembershipCardType.MONTHLY_8);
        client3Monthly8Card.setEntrancesLeft(8);
        client3Monthly8Card.setStartDate(LocalDateTime.now());
        client3Monthly8Card.setEndDate(LocalDateTime.now().plusMonths(1));
        client3Monthly8Card.setPaid(true);
        client3Monthly8Card.setActive(true);
        client3Monthly8Card.setPendingApproval(false);
        client3Monthly8Card.setPrice(new BigDecimal("40.00"));
        client3Monthly8Card.setPurchaseDate(LocalDateTime.now());
        membershipCardRepository.save(client3Monthly8Card);

        MembershipCard client4Monthly12Card = new MembershipCard();
        client4Monthly12Card.setUser(client4);
        client4Monthly12Card.setMembershipCardType(MembershipCardType.MONTHLY_4);
        client4Monthly12Card.setEntrancesLeft(12);
        client4Monthly12Card.setStartDate(LocalDateTime.now());
        client4Monthly12Card.setEndDate(LocalDateTime.now().plusMonths(1));
        client4Monthly12Card.setPaid(true);
        client4Monthly12Card.setActive(true);
        client4Monthly12Card.setPendingApproval(false);
        client4Monthly12Card.setPrice(new BigDecimal("60.00"));
        client4Monthly12Card.setPurchaseDate(LocalDateTime.now());
        membershipCardRepository.save(client4Monthly12Card);

// Seed user event registrations
        UserEventRegistration client1MorningYoga = new UserEventRegistration();
        client1MorningYoga.setUser(client1);
        client1MorningYoga.setEvent(morningYoga);
        client1MorningYoga.setRegistrationDate(LocalDateTime.now());
        client1MorningYoga.setStatus(UserEventRegistrationStatus.REGISTERED);
        userEventRegistrationRepository.save(client1MorningYoga);

        UserEventRegistration client2EveningDance = new UserEventRegistration();
        client2EveningDance.setUser(client2);
        client2EveningDance.setEvent(eveningDance);
        client2EveningDance.setRegistrationDate(LocalDateTime.now());
        client2EveningDance.setStatus(UserEventRegistrationStatus.REGISTERED);
        userEventRegistrationRepository.save(client2EveningDance);

        UserEventRegistration client3PilatesBeginners = new UserEventRegistration();
        client3PilatesBeginners.setUser(client3);
        client3PilatesBeginners.setEvent(pilatesForBeginners);
        client3PilatesBeginners.setRegistrationDate(LocalDateTime.now());
        client3PilatesBeginners.setStatus(UserEventRegistrationStatus.REGISTERED);
        client3PilatesBeginners.setWaitlistPosition(1);
        userEventRegistrationRepository.save(client3PilatesBeginners);

        UserEventRegistration client4CrossFitExtreme = new UserEventRegistration();
        client4CrossFitExtreme.setUser(client4);
        client4CrossFitExtreme.setRegistrationDate(LocalDateTime.now());
        client4CrossFitExtreme.setEvent(crossFitExtreme);
        client4CrossFitExtreme.setStatus(UserEventRegistrationStatus.REGISTERED);
        client4CrossFitExtreme.setWaitlistPosition(2);
        userEventRegistrationRepository.save(client4CrossFitExtreme);

    }
}
