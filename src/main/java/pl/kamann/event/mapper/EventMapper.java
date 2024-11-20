package pl.kamann.event.mapper;

import org.springframework.stereotype.Component;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.user.model.AppUser;

@Component
public class EventMapper {

    public EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .recurring(event.isRecurring())
                .createdById(event.getCreatedBy().getId())
                .instructorId(event.getInstructor().getId())
                .maxParticipants(event.getMaxParticipants())
                .eventTypeName(event.getEventType().getName())
                .status(event.getStatus())
                .build();
    }

    public Event toEntity(EventDto dto, AppUser createdBy, AppUser instructor, EventType eventType) {
        Event event = new Event();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setRecurring(dto.isRecurring());
        event.setCreatedBy(createdBy);
        event.setInstructor(instructor);
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setEventType(eventType);
        event.setStatus(dto.getStatus() != null ? dto.getStatus() : EventStatus.UPCOMING);
        return event;
    }
}