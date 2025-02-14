package pl.kamann.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.EventLightDto;
import pl.kamann.dtos.EventResponse;
import pl.kamann.dtos.EventUpdateResponse;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final EntityLookupService lookupService;

    public EventDto toDto(Event event) {
        int currentParticipants = event.getOccurrences() != null
                ? event.getOccurrences().stream()
                .mapToInt(occ -> occ.getParticipants() != null ? occ.getParticipants().size() : 0)
                .sum()
                : 0;
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .start(event.getStart())
                .durationMinutes(event.getDurationMinutes())
                .updatedAt(event.getUpdatedAt())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .instructorId(event.getInstructor() != null ? event.getInstructor().getId() : null)
                .instructorFullName(event.getInstructor() != null ? event.getInstructor().getFirstName() + " " + event.getInstructor().getLastName() : null)
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .currentParticipants(currentParticipants)
                .eventTypeId(event.getEventType().getId())
                .eventTypeName(event.getEventType() != null ? event.getEventType().getName() : null)
                .createdAt(event.getCreatedAt())
                .build();
    }

//    public EventResponse toResponseDto(Event event) {
//        return new EventResponse(
//                event.getId(),
//                event.getTitle(),
//                event.getDescription(),
//                event.getStart(),
//                event.getDurationMinutes(),
//                event.getRrule(),
//                event.getInstructor() != null ? event.getInstructor().getId() : null,
//                event.getMaxParticipants()
//        );
//    }

    public CreateEventResponse toCreateEventResponse(Event event) {
        return new CreateEventResponse(
                event.getId(),
                event.getTitle(),
                event.getStart(),
                event.getDurationMinutes(),
                event.getStatus()
        );
    }

    public Event toEntity(CreateEventRequest request) {
        return Event.builder()
                .title(request.title())
                .description(request.description())
                .start(request.start())
                .durationMinutes(request.durationMinutes())
                .rrule(request.rrule())
                .createdBy(lookupService.getLoggedInUser())
                .instructor(lookupService.findUserById(request.instructorId()))
                .maxParticipants(request.maxParticipants())
                .eventTypeName(request.eventTypeName())
                .status(EventStatus.SCHEDULED)
                .build();
    }

    public EventLightDto toLightDto(Event event) {
        return new EventLightDto(
                event.getId(),
                event.getTitle(),
                event.getStart(),
                event.getDurationMinutes(),
                event.getStatus(),
                event.getEventType() != null ? event.getEventType().getName() : null
        );
    }

    public EventUpdateResponse toEventUpdateResponse(Event updatedEvent) {
        return new EventUpdateResponse(
                updatedEvent.getId(),
                updatedEvent.getTitle(),
                updatedEvent.getDescription(),
                updatedEvent.getStart(),
                updatedEvent.getDurationMinutes(),
                updatedEvent.getStatus(),
                updatedEvent.getUpdatedAt(),
                updatedEvent.getInstructor() != null ? updatedEvent.getInstructor().getId() : null,
                updatedEvent.getMaxParticipants()
        );
    }
}
