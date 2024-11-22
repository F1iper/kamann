package pl.kamann.attendance.mapper;

import org.junit.jupiter.api.Test;
import pl.kamann.attendance.dto.AttendanceDto;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceMapperTest {

    private final AttendanceMapper mapper = AttendanceMapper.INSTANCE;

    @Test
    void shouldMapEntityToDto() {
        AppUser user = new AppUser();
        user.setId(1L);

        Event event = new Event();
        event.setId(2L);

        Attendance attendance = new Attendance();
        attendance.setId(3L);
        attendance.setUser(user);
        attendance.setEvent(event);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setTimestamp(LocalDateTime.now());

        AttendanceDto dto = mapper.toDto(attendance);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(attendance.getId());
        assertThat(dto.getUserId()).isEqualTo(user.getId());
        assertThat(dto.getEventId()).isEqualTo(event.getId());
        assertThat(dto.getStatus()).isEqualTo(attendance.getStatus());
        assertThat(dto.getTimestamp()).isEqualTo(attendance.getTimestamp());
    }

    @Test
    void shouldMapDtoToEntity() {
        AttendanceDto dto = new AttendanceDto(3L, 1L, 2L, AttendanceStatus.PRESENT, LocalDateTime.now());

        Attendance attendance = mapper.toEntity(dto);

        assertThat(attendance).isNotNull();
        assertThat(attendance.getId()).isEqualTo(dto.getId());
        assertThat(attendance.getStatus()).isEqualTo(dto.getStatus());
        assertThat(attendance.getTimestamp()).isEqualTo(dto.getTimestamp());
        assertThat(attendance.getUser()).isNull();
        assertThat(attendance.getEvent()).isNull();
    }
}
