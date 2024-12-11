package pl.kamann.services.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.event.Event;
import pl.kamann.repositories.UserEventHistoryRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

class ClientEventHistoryServiceTest {

    @InjectMocks
    private ClientEventHistoryService clientEventHistoryService;

    @Mock
    private UserEventHistoryRepository userEventHistoryRepository;

    @Mock
    private EntityLookupService lookupService;

    private AppUser mockUser;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new AppUser();
        mockUser.setId(1L);

        mockEvent = new Event();
        mockEvent.setId(1L);
    }

    @Test
    void testUpdateEventHistoryWithExistingRecord() {
        var existingHistory = new ClientEventHistory();
        when(lookupService.findEventById(1L)).thenReturn(mockEvent);
        when(userEventHistoryRepository.findByUserAndEvent(mockUser, mockEvent)).thenReturn(Optional.of(existingHistory));

        clientEventHistoryService.updateEventHistory(mockUser, 1L, AttendanceStatus.PRESENT);

        verify(lookupService, times(1)).findEventById(1L);
        verify(userEventHistoryRepository, times(1)).findByUserAndEvent(mockUser, mockEvent);
        verify(userEventHistoryRepository, times(1)).save(existingHistory);
    }

    @Test
    void testUpdateEventHistoryWithNewRecord() {
        when(lookupService.findEventById(1L)).thenReturn(mockEvent);
        when(userEventHistoryRepository.findByUserAndEvent(mockUser, mockEvent)).thenReturn(Optional.empty());

        clientEventHistoryService.updateEventHistory(mockUser, 1L, AttendanceStatus.PRESENT);

        verify(lookupService, times(1)).findEventById(1L);
        verify(userEventHistoryRepository, times(1)).findByUserAndEvent(mockUser, mockEvent);
        verify(userEventHistoryRepository, times(1)).save(any(ClientEventHistory.class));
    }

    @Test
    void testLogEventHistoryPresent() {
        clientEventHistoryService.logEventHistory(mockUser, mockEvent, AttendanceStatus.PRESENT);

        verify(userEventHistoryRepository, times(1)).save(any(ClientEventHistory.class));
    }

    @Test
    void testLogEventHistoryAbsent() {
        clientEventHistoryService.logEventHistory(mockUser, mockEvent, AttendanceStatus.ABSENT);

        verify(userEventHistoryRepository, times(1)).save(any(ClientEventHistory.class));
    }
}
