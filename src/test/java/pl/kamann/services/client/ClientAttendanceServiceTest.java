package pl.kamann.services.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.systemevents.EventHistoryLogEvent;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAttendanceServiceTest {

    @InjectMocks
    private ClientAttendanceService clientAttendanceService;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EntityLookupService lookupService;
    @Mock
    private ClientEventHistoryService clientEventHistoryService;
    @Mock
    private ClientMembershipCardService clientMembershipCardService;

    @Mock
    private ApplicationEventPublisher eventPublisher;


    private AppUser mockUser;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        mockUser = new AppUser();
        mockUser.setId(1L);

        mockEvent = new Event();
        mockEvent.setId(100L);
        mockEvent.setStartTime(LocalDateTime.now().plusDays(2));
    }

    @Test
    void joinEventWhenNotAlreadyRegisteredShouldRegister() {
        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
        when(lookupService.findEventById(mockEvent.getId())).thenReturn(mockEvent);
        when(attendanceRepository.findByUserAndEvent(mockUser, mockEvent)).thenReturn(Optional.empty());

        Attendance result = clientAttendanceService.joinEvent(mockEvent.getId());

        verify(clientMembershipCardService).deductEntry(mockUser.getId());
        ArgumentCaptor<Attendance> attendanceCaptor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(attendanceCaptor.capture());

        ArgumentCaptor<EventHistoryLogEvent> eventCaptor = ArgumentCaptor.forClass(EventHistoryLogEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EventHistoryLogEvent publishedEvent = eventCaptor.getValue();
        assertAll(
                () -> assertThat(publishedEvent.getUser()).isEqualTo(mockUser),
                () -> assertThat(publishedEvent.getEvent()).isEqualTo(mockEvent),
                () -> assertThat(publishedEvent.getStatus()).isEqualTo(AttendanceStatus.REGISTERED)
        );

        Attendance saved = attendanceCaptor.getValue();
        assertAll(
                () -> assertThat(saved.getUser()).isEqualTo(mockUser),
                () -> assertThat(saved.getEvent()).isEqualTo(mockEvent),
                () -> assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.REGISTERED),
                () -> assertThat(result).isEqualTo(saved)
        );
    }

    @Test
    void joinEventWhenAlreadyRegisteredShouldThrowApiException() {
        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
        when(lookupService.findEventById(mockEvent.getId())).thenReturn(mockEvent);
        var existingAttendance = new Attendance();
        existingAttendance.setUser(mockUser);
        existingAttendance.setEvent(mockEvent);
        existingAttendance.setStatus(AttendanceStatus.REGISTERED);

        when(attendanceRepository.findByUserAndEvent(mockUser, mockEvent))
                .thenReturn(Optional.of(existingAttendance));

        assertThatThrownBy(() -> clientAttendanceService.joinEvent(mockEvent.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("status", "code")
                .containsExactly(HttpStatus.CONFLICT, AttendanceCodes.ALREADY_REGISTERED.name());
    }

    @Test
    void cancelAttendanceWhenAttendanceExistsAndIsNotPresentShouldCancel() {
        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
        when(lookupService.findEventById(mockEvent.getId())).thenReturn(mockEvent);
        when(lookupService.findUserById(mockUser.getId())).thenReturn(mockUser);

        var existingAttendance = new Attendance();
        existingAttendance.setUser(mockUser);
        existingAttendance.setEvent(mockEvent);
        existingAttendance.setStatus(AttendanceStatus.REGISTERED);

        when(attendanceRepository.findByUserAndEvent(mockUser, mockEvent))
                .thenReturn(Optional.of(existingAttendance));

        Attendance result = clientAttendanceService.cancelAttendance(mockEvent.getId());

        ArgumentCaptor<Attendance> attendanceCaptor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(attendanceCaptor.capture());
        Attendance saved = attendanceCaptor.getValue();

        assertAll(
                () -> assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.EARLY_CANCEL),
                () -> assertThat(result).isEqualTo(saved)
        );

        ArgumentCaptor<EventHistoryLogEvent> eventCaptor = ArgumentCaptor.forClass(EventHistoryLogEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EventHistoryLogEvent publishedEvent = eventCaptor.getValue();
        assertAll(
                () -> assertThat(publishedEvent.getUser()).isEqualTo(mockUser),
                () -> assertThat(publishedEvent.getEvent()).isEqualTo(mockEvent),
                () -> assertThat(publishedEvent.getStatus()).isEqualTo(AttendanceStatus.EARLY_CANCEL)
        );
    }

    @Test
    void cancelAttendanceWhenAttendanceNotFoundShouldThrow() {
        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
        when(lookupService.findEventById(mockEvent.getId())).thenReturn(mockEvent);
        when(lookupService.findUserById(mockUser.getId())).thenReturn(mockUser);
        when(attendanceRepository.findByUserAndEvent(mockUser, mockEvent)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientAttendanceService.cancelAttendance(mockEvent.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("status", "code")
                .containsExactly(HttpStatus.NOT_FOUND, AttendanceCodes.ATTENDANCE_NOT_FOUND.name());
    }

    @Test
    void cancelAttendanceWhenAttendanceIsPresentShouldThrow() {
        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
        when(lookupService.findEventById(mockEvent.getId())).thenReturn(mockEvent);
        when(lookupService.findUserById(mockUser.getId())).thenReturn(mockUser);
        var existingAttendance = new Attendance();
        existingAttendance.setUser(mockUser);
        existingAttendance.setEvent(mockEvent);
        existingAttendance.setStatus(AttendanceStatus.PRESENT);

        when(attendanceRepository.findByUserAndEvent(mockUser, mockEvent))
                .thenReturn(Optional.of(existingAttendance));

        assertThatThrownBy(() -> clientAttendanceService.cancelAttendance(mockEvent.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("status", "code")
                .containsExactly(HttpStatus.BAD_REQUEST, AttendanceCodes.INVALID_ATTENDANCE_STATE.name());
    }

    @Test
    void getAttendanceSummaryShouldThrowUnsupportedOperationException() {
        assertThatThrownBy(() -> clientAttendanceService.getAttendanceSummary())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
