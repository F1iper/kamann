package pl.kamann.event.registration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.*;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.UserEventRegistrationRepository;
import pl.kamann.services.client.ClientEventHistoryService;
import pl.kamann.services.client.ClientEventRegistrationService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientEventRegistrationServiceTest {

    @Mock
    private UserEventRegistrationRepository registrationRepository;

    @Mock
    private EntityLookupService lookupService;

    @Mock
    private ClientEventHistoryService clientEventHistoryService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ClientEventHistoryService eventHistoryService;

    @InjectMocks
    private ClientEventRegistrationService registrationService;

    private AppUser user;
    private Event event;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new AppUser();
        user.setId(1L);
        user.setEmail("user@test.com");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setMaxParticipants(2);
    }

//    @Test
//    void registerUserForEventShouldRegisterSuccessfullyWhenSlotsAvailable() {
//        when(lookupService.findUserById(1L)).thenReturn(user);
//        when(lookupService.findEventById(1L)).thenReturn(event);
//        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
//        when(registrationRepository.countByEventAndStatus(event, UserEventRegistrationStatus.REGISTERED)).thenReturn(0);
//
//        registrationService.registerUserForEvent(1L, 1L);
//
//        ArgumentCaptor<UserEventRegistration> registrationCaptor = ArgumentCaptor.forClass(UserEventRegistration.class);
//        verify(registrationRepository).save(registrationCaptor.capture());
//
//        UserEventRegistration capturedRegistration = registrationCaptor.getValue();
//        assertEquals(user, capturedRegistration.getUser());
//        assertEquals(event, capturedRegistration.getEvent());
//        assertEquals(UserEventRegistrationStatus.REGISTERED, capturedRegistration.getStatus());
//
//        // Verify logEventHistory with exact arguments
//        verify(clientEventHistoryService).logEventHistory(eq(user), eq(event), eq(AttendanceStatus.REGISTERED));
//    }

//    @Test
//    void registerUserForEventShouldAddToWaitlistWhenEventIsFull() {
//        when(lookupService.findUserById(1L)).thenReturn(user);
//        when(lookupService.findEventById(1L)).thenReturn(event);
//
//        when(registrationRepository.countByEventAndStatus(event, UserEventRegistrationStatus.REGISTERED)).thenReturn(2);
//
//        when(registrationRepository.countByEventAndWaitlistPositionIsNotNull(event)).thenReturn(1);
//
//        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
//
//        registrationService.registerUserForEvent(1L, 1L);
//
//        ArgumentCaptor<UserEventRegistration> registrationCaptor = ArgumentCaptor.forClass(UserEventRegistration.class);
//        verify(registrationRepository).save(registrationCaptor.capture());
//
//        UserEventRegistration capturedRegistration = registrationCaptor.getValue();
//        assertEquals(UserEventRegistrationStatus.WAITLISTED, capturedRegistration.getStatus());
//        assertEquals(2, capturedRegistration.getWaitlistPosition()); // Second in the waitlist
//
//        verify(eventHistoryService).logEventHistory(user, event, AttendanceStatus.WAITLISTED);
//    }

    @Test
    void registerUserForEventShouldThrowExceptionWhenUserAlreadyRegistered() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> registrationService.registerUserForEvent(1L, 1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(Codes.ALREADY_REGISTERED, exception.getCode());

        verify(eventHistoryService, never()).logEventHistory(any(), any(), any());
    }

//    @Test
//    void cancelUserRegistrationShouldCancelSuccessfullyWhenRegistrationExists() {
//        UserEventRegistration registration = new UserEventRegistration();
//        registration.setUser(user);
//        registration.setEvent(event);
//        registration.setStatus(UserEventRegistrationStatus.REGISTERED);
//
//        when(lookupService.findUserById(1L)).thenReturn(user);
//        when(lookupService.findEventById(1L)).thenReturn(event);
//        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(registration));
//
//        registrationService.cancelUserRegistration(1L, 1L);
//
//        verify(registrationRepository).save(registration);
//        assertEquals(UserEventRegistrationStatus.CANCELLED, registration.getStatus());
//
//        verify(eventHistoryService).logEventHistory(user, event, AttendanceStatus.CANCELED_BY_CLIENT);
//    }

    @Test
    void cancelUserRegistrationShouldThrowExceptionWhenRegistrationNotFound() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> registrationService.cancelUserRegistration(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(Codes.ATTENDANCE_NOT_FOUND, exception.getCode());

        verify(eventHistoryService, never()).logEventHistory(any(), any(), any());
    }

//    @Test
//    void handleWaitlistShouldPromoteUserWhenWaitlistExists() {
//        UserEventRegistration waitlistedUser = new UserEventRegistration();
//        waitlistedUser.setUser(user);
//        waitlistedUser.setEvent(event);
//        waitlistedUser.setStatus(UserEventRegistrationStatus.WAITLISTED);
//        waitlistedUser.setWaitlistPosition(1);
//
//        when(lookupService.findEventById(1L)).thenReturn(event);
//        when(registrationRepository.findFirstByEventAndStatusOrderByWaitlistPositionAsc(event, UserEventRegistrationStatus.WAITLISTED))
//                .thenReturn(waitlistedUser);
//
//        registrationService.handleWaitlist(1L);
//
//        verify(registrationRepository).save(waitlistedUser);
//        assertEquals(UserEventRegistrationStatus.REGISTERED, waitlistedUser.getStatus());
//        assertNull(waitlistedUser.getWaitlistPosition());
//
//        verify(eventHistoryService).logEventHistory(user, event, AttendanceStatus.REGISTERED);
//    }

    @Test
    void handleWaitlistShouldDoNothingWhenNoWaitlistedUser() {
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findFirstByEventAndStatusOrderByWaitlistPositionAsc(event, UserEventRegistrationStatus.WAITLISTED))
                .thenReturn(null);

        registrationService.handleWaitlist(1L);

        verify(registrationRepository, never()).save(any());

        verify(eventHistoryService, never()).logEventHistory(any(), any(), any());
    }
}
