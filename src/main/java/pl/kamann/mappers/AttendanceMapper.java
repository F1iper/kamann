package pl.kamann.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kamann.dtos.AttendanceDetailsDto;
import pl.kamann.entities.attendance.Attendance;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    @Mapping(target = "occurrenceEventId", source = "occurrenceEvent.id")
    @Mapping(target = "userId", source = "user.id")
    AttendanceDetailsDto toAttendanceDetailsDto(Attendance attendance);

  }
