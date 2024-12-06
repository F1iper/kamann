package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;

@Component
public class EventMapper {

    public EventDto toDto(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .recurring(event.isRecurring())
                .maxParticipants(event.getMaxParticipants())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .instructorId(event.getInstructor() != null ? event.getInstructor().getId() : null)
                .eventTypeId(event.getEventType() != null ? event.getEventType().getId() : null)
                .eventTypeName(event.getEventType() != null ? event.getEventType().getName() : null)
                .status(event.getStatus())
                .build();
    }

    public Event toEntity(EventDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("EventDto cannot be null");
        }

        return Event.builder()
                .id(dto.id())
                .title(dto.title())
                .description(dto.description())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .recurring(dto.recurring())
                .maxParticipants(dto.maxParticipants())
                .status(dto.status() != null ? dto.status() : EventStatus.SCHEDULED)
                .build();
    }

    public void updateEventFromDto(Event existingEvent, EventDto updatedEventDto) {
        if (existingEvent == null || updatedEventDto == null) {
            throw new IllegalArgumentException("ExistingEvent and UpdatedEventDto cannot be null");
        }

        if (updatedEventDto.title() != null) {
            existingEvent.setTitle(updatedEventDto.title());
        }
        if (updatedEventDto.description() != null) {
            existingEvent.setDescription(updatedEventDto.description());
        }
        if (updatedEventDto.startTime() != null) {
            existingEvent.setStartTime(updatedEventDto.startTime());
        }
        if (updatedEventDto.endTime() != null) {
            existingEvent.setEndTime(updatedEventDto.endTime());
        }

        existingEvent.setRecurring(updatedEventDto.recurring());
        existingEvent.setMaxParticipants(updatedEventDto.maxParticipants());

        if (updatedEventDto.status() != null) {
            existingEvent.setStatus(updatedEventDto.status());
        }
    }
}
