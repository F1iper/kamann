package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.AttendanceDto;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.appuser.AppUser;

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
