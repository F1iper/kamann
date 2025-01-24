package pl.kamann.event.registration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.UserEventRegistrationStatus;
import pl.kamann.repositories.UserEventRegistrationRepository;
import pl.kamann.services.client.ClientEventHistoryService;
import pl.kamann.services.client.ClientEventRegistrationService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClientEventRegistrationServiceTest {

    @Mock
    private UserEventRegistrationRepository registrationRepository;

    @Mock
    private EntityLookupService lookupService;

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

    @Test
    void registerUserForEventShouldThrowExceptionWhenUserAlreadyRegistered() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> registrationService.registerUserForEvent(1L, 1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(AttendanceCodes.ALREADY_REGISTERED.name(), exception.getCode());

        verify(eventHistoryService, never()).logEventHistory(any(), any(), any());
    }

    @Test
    void cancelUserRegistrationShouldThrowExceptionWhenRegistrationNotFound() {
        when(lookupService.findUserById(1L)).thenReturn(user);
        when(lookupService.findEventById(1L)).thenReturn(event);
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> registrationService.cancelUserRegistration(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(AttendanceCodes.ATTENDANCE_NOT_FOUND.name(), exception.getCode());

        verify(eventHistoryService, never()).logEventHistory(any(), any(), any());
    }

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
