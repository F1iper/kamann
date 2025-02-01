package pl.kamann.services.instructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.config.codes.AttendanceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.services.client.ClientEventHistoryService;
import pl.kamann.services.client.ClientMembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InstructorAttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EntityLookupService lookupService;

    @Mock
    private ClientMembershipCardService clientMembershipCardService;

    @Mock
    private ClientEventHistoryService clientEventHistoryService;

    @InjectMocks
    private InstructorAttendanceService instructorAttendanceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cancelClientAttendanceShouldSetEarlyCancelStatusWhenCancelledEarly() {
        Long eventId = 1L;
        Long clientId = 1L;

        AppUser user = new AppUser();
        Event event = new Event();
        event.setStartDate(LocalDate.now().plusDays(2));
        event.setTime(LocalTime.of(15, 0));

        Attendance attendance = new Attendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.REGISTERED);

        when(lookupService.findUserById(clientId)).thenReturn(user);
        when(lookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));

        instructorAttendanceService.cancelClientAttendance(eventId, clientId);

        assertEquals(AttendanceStatus.EARLY_CANCEL, attendance.getStatus());
        verify(attendanceRepository).save(attendance);
        verify(clientEventHistoryService).logEventHistory(user, event, AttendanceStatus.EARLY_CANCEL);
    }

    @Test
    void cancelClientAttendanceShouldSetLateCancelStatusWhenCancelledLate() {
        Long eventId = 1L;
        Long clientId = 1L;

        AppUser user = new AppUser();
        Event event = new Event();
        event.setStartDate(LocalDate.now().plusDays(1));
        event.setTime(LocalTime.of(10, 0));

        Attendance attendance = new Attendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.REGISTERED);

        when(lookupService.findUserById(clientId)).thenReturn(user);
        when(lookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));

        instructorAttendanceService.cancelClientAttendance(eventId, clientId);

        assertEquals(AttendanceStatus.LATE_CANCEL, attendance.getStatus());
        verify(attendanceRepository).save(attendance);
        verify(clientEventHistoryService).logEventHistory(user, event, AttendanceStatus.LATE_CANCEL);
    }

    @Test
    void cancelClientAttendanceShouldThrowExceptionWhenStatusIsPresent() {
        Long eventId = 1L;
        Long clientId = 1L;

        AppUser user = new AppUser();
        Event event = new Event();

        Attendance attendance = new Attendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.PRESENT);

        when(lookupService.findUserById(clientId)).thenReturn(user);
        when(lookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));

        ApiException exception = assertThrows(ApiException.class, () ->
                instructorAttendanceService.cancelClientAttendance(eventId, clientId));
        assertEquals("Cannot cancel attendance already marked as PRESENT.", exception.getMessage());
        assertEquals(AttendanceCodes.INVALID_ATTENDANCE_STATE.name(), exception.getCode());
    }

    @Test
    void markAttendanceShouldDeductEntryWhenStatusIsPresent() {
        Long eventId = 1L;
        Long clientId = 1L;

        AppUser user = new AppUser();
        Event event = new Event();

        Attendance attendance = new Attendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.REGISTERED);

        when(lookupService.findUserById(clientId)).thenReturn(user);
        when(lookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));

        instructorAttendanceService.markAttendance(eventId, clientId, AttendanceStatus.PRESENT);

        verify(clientMembershipCardService).deductEntry(clientId);
        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void markAttendanceShouldThrowExceptionWhenStatusIsCanceledByInstructor() {
        Long eventId = 1L;
        Long clientId = 1L;

        AppUser user = new AppUser();
        Event event = new Event();

        Attendance attendance = new Attendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(AttendanceStatus.CANCELED_BY_INSTRUCTOR);

        when(lookupService.findUserById(clientId)).thenReturn(user);
        when(lookupService.findEventById(eventId)).thenReturn(event);
        when(attendanceRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));

        ApiException exception = assertThrows(ApiException.class, () ->
                instructorAttendanceService.markAttendance(eventId, clientId, AttendanceStatus.PRESENT));
        assertEquals("Cannot mark attendance for a canceled status. Current status: CANCELED_BY_INSTRUCTOR", exception.getMessage());
        assertEquals(AttendanceCodes.INVALID_ATTENDANCE_STATE.name(), exception.getCode());
    }

    @Test
    void cancelClientAttendanceShouldThrowExceptionWhenAttendanceNotFound() {
        Long eventId = 1L;
        Long clientId = 1L;

        when(lookupService.findUserById(clientId)).thenReturn(new AppUser());
        when(lookupService.findEventById(eventId)).thenReturn(new Event());
        when(attendanceRepository.findByUserAndEvent(any(), any())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                instructorAttendanceService.cancelClientAttendance(eventId, clientId));
        assertEquals("Attendance not found for user: 1 and event: 1", exception.getMessage());
        assertEquals(AttendanceCodes.ATTENDANCE_NOT_FOUND.name(), exception.getCode());
    }

    @Test
    void listAttendancesForEventShouldReturnAttendancesForLoggedInInstructor() {
        Long eventId = 1L;
        AppUser instructor = new AppUser();
        instructor.setId(10L);

        List<Attendance> attendances = List.of(new Attendance(), new Attendance());

        when(lookupService.getLoggedInUser()).thenReturn(instructor);
        when(attendanceRepository.findAllByEventAndInstructor(eventId, instructor.getId())).thenReturn(attendances);

        var result = instructorAttendanceService.listAttendancesForEvent(eventId);

        assertEquals(attendances.size(), result.size());
        verify(attendanceRepository).findAllByEventAndInstructor(eventId, instructor.getId());
    }
}
