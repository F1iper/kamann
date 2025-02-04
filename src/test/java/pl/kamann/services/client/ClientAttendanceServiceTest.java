package pl.kamann.services.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ClientAttendanceServiceTest {

    @InjectMocks
    private ClientAttendanceService clientAttendanceService;

    private AppUser mockUser;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        mockUser = new AppUser();
        mockUser.setId(1L);

        mockEvent = new Event();
        mockEvent.setId(100L);
        mockEvent.setStartTime(LocalTime.of(9, 0));
        mockEvent.setEndTime(LocalTime.of(10, 0));
    }

    @Test
    void getAttendanceSummaryShouldThrowUnsupportedOperationException() {
        assertThatThrownBy(() -> clientAttendanceService.getAttendanceSummary())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
