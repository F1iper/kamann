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
import pl.kamann.event.model.Event;
import pl.kamann.registration.model.UserEventRegistration;
import pl.kamann.registration.model.UserEventRegistrationStatus;
import pl.kamann.registration.repository.UserEventRegistrationRepository;
import pl.kamann.registration.service.ClientEventRegistrationService;
import pl.kamann.user.model.AppUser;
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

    @Test
    void registerUserForEvent_ShouldRegisterSuccessfully_WhenSlotsAvailable() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.countByEventAndStatus(event, UserEventRegistrationStatus.REGISTERED)).thenReturn(1);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);

        boolean result = registrationService.registerUserForEvent(1L, 1L);

        assertTrue(result);
        ArgumentCaptor<UserEventRegistration> registrationCaptor = ArgumentCaptor.forClass(UserEventRegistration.class);
        verify(registrationRepository).save(registrationCaptor.capture());
        assertEquals(UserEventRegistrationStatus.REGISTERED, registrationCaptor.getValue().getStatus());
    }

    @Test
    void registerUserForEvent_ShouldAddToWaitlist_WhenEventIsFull() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.countByEventAndStatus(event, UserEventRegistrationStatus.REGISTERED)).thenReturn(2);
        when(registrationRepository.countByEventAndWaitlistPositionIsNotNull(event)).thenReturn(1);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);

        boolean result = registrationService.registerUserForEvent(1L, 1L);

        assertTrue(result);
        ArgumentCaptor<UserEventRegistration> registrationCaptor = ArgumentCaptor.forClass(UserEventRegistration.class);
        verify(registrationRepository).save(registrationCaptor.capture());
        assertEquals(UserEventRegistrationStatus.WAITLISTED, registrationCaptor.getValue().getStatus());
        assertEquals(2, registrationCaptor.getValue().getWaitlistPosition());
    }

    @Test
    void registerUserForEvent_ShouldThrowException_WhenUserAlreadyRegistered() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> registrationService.registerUserForEvent(1L, 1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(Codes.ALREADY_REGISTERED, exception.getCode());
    }

    @Test
    void cancelUserRegistration_ShouldCancelSuccessfully_WhenRegistrationExists() {
        UserEventRegistration registration = new UserEventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(UserEventRegistrationStatus.REGISTERED);

        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(registration));

        boolean result = registrationService.cancelUserRegistration(1L, 1L);

        assertTrue(result);
        verify(registrationRepository).save(registration);
        assertEquals(UserEventRegistrationStatus.CANCELLED, registration.getStatus());
    }

    @Test
    void cancelUserRegistration_ShouldThrowException_WhenRegistrationNotFound() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> registrationService.cancelUserRegistration(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(Codes.REGISTRATION_NOT_FOUND, exception.getCode());
    }

    @Test
    void handleWaitlist_ShouldPromoteUser_WhenWaitlistExists() {
        UserEventRegistration waitlistedUser = new UserEventRegistration();
        waitlistedUser.setUser(user);
        waitlistedUser.setEvent(event);
        waitlistedUser.setStatus(UserEventRegistrationStatus.WAITLISTED);
        waitlistedUser.setWaitlistPosition(1);

        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findFirstByEventAndStatusOrderByWaitlistPositionAsc(event, UserEventRegistrationStatus.WAITLISTED))
                .thenReturn(waitlistedUser);

        registrationService.handleWaitlist(1L);

        verify(registrationRepository).save(waitlistedUser);
        assertEquals(UserEventRegistrationStatus.REGISTERED, waitlistedUser.getStatus());
        assertNull(waitlistedUser.getWaitlistPosition());
    }

    @Test
    void handleWaitlist_ShouldDoNothing_WhenNoWaitlistedUser() {
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findFirstByEventAndStatusOrderByWaitlistPositionAsc(event, UserEventRegistrationStatus.WAITLISTED))
                .thenReturn(null);

        registrationService.handleWaitlist(1L);

        verify(registrationRepository, never()).save(any());
    }
}
