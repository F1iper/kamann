package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.entities.EventDto;
import pl.kamann.entities.Event;
import pl.kamann.entities.EventStatus;
import pl.kamann.entities.EventType;
import pl.kamann.entities.AppUser;

@Component
public class EventMapper {

    public Event toEntity(EventDto dto, AppUser createdBy, AppUser instructor, EventType eventType) {
        return Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .recurring(dto.isRecurring())
                .maxParticipants(dto.getMaxParticipants())
                .createdBy(createdBy)
                .instructor(instructor)
                .eventType(eventType)
                .status(dto.getStatus() != null ? dto.getStatus() : EventStatus.SCHEDULED)
                .build();
    }

    public EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .recurring(event.isRecurring())
                .maxParticipants(event.getMaxParticipants())
                .createdById(event.getCreatedBy().getId())
                .instructorId(event.getInstructor().getId())
                .eventTypeId(event.getEventType().getId())
                .eventTypeName(event.getEventType().getName())
                .status(event.getStatus())
                .build();
    }

    public void updateEventFromDto(Event existingEvent, EventDto updatedEventDto, AppUser instructor, EventType eventType) {
        existingEvent.setTitle(updatedEventDto.getTitle());
        existingEvent.setDescription(updatedEventDto.getDescription());
        existingEvent.setStartTime(updatedEventDto.getStartTime());
        existingEvent.setEndTime(updatedEventDto.getEndTime());
        existingEvent.setRecurring(updatedEventDto.isRecurring());
        existingEvent.setMaxParticipants(updatedEventDto.getMaxParticipants());
        existingEvent.setInstructor(instructor);
        existingEvent.setEventType(eventType);

        if (updatedEventDto.getStatus() != null) {
            existingEvent.setStatus(updatedEventDto.getStatus());
        }
    }
}