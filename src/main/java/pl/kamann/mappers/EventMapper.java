package pl.kamann.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.EventResponseDto;
import pl.kamann.entities.event.Event;

@Component
@RequiredArgsConstructor
public class EventMapper {

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
                .rrule(event.getRrule())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .instructorId(event.getInstructor() != null ? event.getInstructor().getId() : null)
                .instructorFullName(event.getInstructor() != null ? event.getInstructor().getFirstName() + " " + event.getInstructor().getLastName() : null)
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .currentParticipants(currentParticipants)
                .eventTypeId(event.getEventType() != null ? event.getEventType().getId() : null)
                .eventTypeName(event.getEventType() != null ? event.getEventType().getName() : null)
                .build();
    }

    public Event toEntity(EventDto dto) {
        return Event.builder()
                .id(dto.id())
                .title(dto.title())
                .description(dto.description())
                .start(dto.start())
                .durationMinutes(dto.durationMinutes())
                .rrule(dto.rrule())
                .status(dto.status())
                .maxParticipants(dto.maxParticipants())
                .build();
    }

    public EventResponseDto toResponseDto(Event event) {
        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStart(),
                event.getDurationMinutes(),
                event.getRrule(),
                event.getInstructor() != null ? event.getInstructor().getId() : null,
                event.getMaxParticipants()
        );
    }
}
