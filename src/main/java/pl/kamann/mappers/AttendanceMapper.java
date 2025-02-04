package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.AttendanceDetailsDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.OccurrenceEvent;

@Component
public class AttendanceMapper {

    public AttendanceDetailsDto toDto(Attendance attendance) {
        return AttendanceDetailsDto.builder()
                .id(attendance.getId())
                .occurrenceEventId(attendance.getOccurrenceEvent().getId())
                .userId(attendance.getUser().getId())
                .status(attendance.getStatus())
                .build();
    }

    public Attendance toEntity(AttendanceDetailsDto dto, OccurrenceEvent event, AppUser user) {
        return Attendance.builder()
                .id(dto.id())
                .occurrenceEvent(event)
                .user(user)
                .status(dto.status())
                .build();
    }
}
