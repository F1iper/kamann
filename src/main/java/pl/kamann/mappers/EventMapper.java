package pl.kamann.mappers;

import pl.kamann.dtos.event.EventDto;
import pl.kamann.dtos.event.EventLightDto;
import pl.kamann.dtos.event.EventUpdateResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.entities.event.Event;
import pl.kamann.utility.EntityLookupService;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "instructorFullName", expression = "java(mapInstructorFullName(event))")
    @Mapping(target = "currentParticipants", expression = "java(calculateCurrentParticipants(event))")
    @Mapping(target = "eventTypeId", source = "eventType.id")
    @Mapping(target = "eventTypeName", source = "eventType.name")
    EventDto toEventDto(Event event);

    default String mapInstructorFullName(Event event) {
        return event.getInstructor() != null ? event.getInstructor().getFirstName() + " " + event.getInstructor().getLastName() : null;
    }

    default int calculateCurrentParticipants(Event event) {
        return event.getOccurrences() != null
                ? event.getOccurrences().stream()
                .mapToInt(occ -> occ.getParticipants() != null ? occ.getParticipants().size() : 0)
                .sum()
                : 0;
    }

    @Mapping(target = "createdBy", expression = "java(lookupService.getLoggedInUser())")
    @Mapping(target = "instructor", expression = "java(lookupService.findUserById(request.instructorId()))")
    @Mapping(target = "status", expression = "java(EventStatus.SCHEDULED)")
    Event toEvent(CreateEventRequest request, @Context EntityLookupService lookupService);

    CreateEventResponse toCreateEventResponse(Event event);

    EventLightDto toEventLightDto(Event event);

    @Mapping(target = "instructorId", source = "instructor.id")
    EventUpdateResponse toEventUpdateResponse(Event event);
}
