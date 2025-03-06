package pl.kamann.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kamann.dtos.AttendanceDetailsDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.OccurrenceEvent;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    @Mapping(target = "occurrenceEventId", source = "occurrenceEvent.id")
    @Mapping(target = "userId", source = "user.id")
    AttendanceDetailsDto toAttendanceDetailsDto(Attendance attendance);

    @Mapping(target = "id", source = "attendanceDetailsDto.id")
    @Mapping(target = "status", source = "attendanceDetailsDto.status")
    @Mapping(target = "user", source = "appUser")
    Attendance toAttendance(AttendanceDetailsDto attendanceDetailsDto, OccurrenceEvent occurrenceEvent, AppUser appUser);
}
