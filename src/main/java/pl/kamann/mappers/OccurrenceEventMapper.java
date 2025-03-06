package pl.kamann.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.entities.event.OccurrenceEvent;

@Mapper(componentModel = "spring")
public interface OccurrenceEventMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "date", expression = "java(occurrenceEvent.getStart().toLocalDate())")
    @Mapping(target = "startTime", expression = "java(occurrenceEvent.getStart().toLocalTime())")
    @Mapping(target = "endTime", expression = "java(occurrenceEvent.getEnd().toLocalTime())")
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "eventTypeName", source = "event.eventType.name")
    @Mapping(target = "instructorFullName", expression = "java(mapInstructorFullName(occurrenceEvent))")
    @Mapping(target = "attendanceCount", expression = "java(occurrenceEvent.getAttendances().size())")
    @Mapping(target = "isModified", expression = "java(occurrenceEvent.isModified())")
    OccurrenceEventDto toOccurrenceEventDto(OccurrenceEvent occurrenceEvent);

    @Mapping(target = "occurrenceId", source = "id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "start", source = "start")
    @Mapping(target = "end", expression = "java(occurrenceEvent.getStart().plusMinutes(occurrenceEvent.getDurationMinutes()))")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "instructorName", expression = "java(mapInstructorFullName(occurrenceEvent))")
    @Mapping(target = "eventTypeName", source = "event.eventTypeName")
    OccurrenceEventLightDto toOccurrenceEventLightDto(OccurrenceEvent occurrenceEvent);

    default String mapInstructorFullName(OccurrenceEvent occurrenceEvent) {
        return occurrenceEvent.getInstructor() != null ? occurrenceEvent.getInstructor().getFirstName() + " " + occurrenceEvent.getInstructor().getLastName() : null;
    }
}
