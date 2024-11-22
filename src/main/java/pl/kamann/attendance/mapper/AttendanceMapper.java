package pl.kamann.attendance.mapper;

import org.springframework.stereotype.Component;
import pl.kamann.attendance.dto.AttendanceDto;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.event.model.Event;
import pl.kamann.user.model.AppUser;

@Component
public class AttendanceMapper {

    public AttendanceDto toDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .eventId(attendance.getEvent().getId())
                .status(attendance.getStatus())
                .timestamp(attendance.getTimestamp())
                .build();
    }

    public Attendance toEntity(AttendanceDto dto, AppUser user, Event event) {
        return new Attendance(
                dto.getId(),
                user,
                event,
                dto.getStatus(),
                dto.getTimestamp()
        );
    }
}
