package pl.kamann.event.mapper;

import org.springframework.stereotype.Component;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventType;
import pl.kamann.user.model.AppUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public EventDto toDto(Event event, List<Attendance> attendances) {
        List<Attendance> attendanceList = Optional.ofNullable(attendances).orElse(List.of());

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
                .eventTypeId(event.getEventType().getId())
                .eventTypeName(event.getEventType().getName())
                .status(event.getStatus())
                .attendanceSummary(attendanceList.size())
                .build();
    }

    public Event toEntity(EventDto dto, AppUser createdBy, AppUser instructor, EventType eventType) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setRecurring(dto.isRecurring());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setStatus(dto.getStatus());
        event.setCreatedBy(createdBy);
        event.setInstructor(instructor);
        event.setEventType(eventType);
        return event;
    }

    public void updateEventFromDto(Event existingEvent, EventDto updatedEventDto, AppUser instructor, EventType eventType) {
        existingEvent.setTitle(updatedEventDto.getTitle());
        existingEvent.setDescription(updatedEventDto.getDescription());
        existingEvent.setStartTime(updatedEventDto.getStartTime());
        existingEvent.setEndTime(updatedEventDto.getEndTime());
        existingEvent.setRecurring(updatedEventDto.isRecurring());
        existingEvent.setMaxParticipants(updatedEventDto.getMaxParticipants());
        existingEvent.setStatus(updatedEventDto.getStatus());
        existingEvent.setInstructor(instructor);
        existingEvent.setEventType(eventType);
    }

    public List<EventDto> toDtoList(List<Event> events) {
        return events.stream()
                .map(event -> toDto(event, null))
                .collect(Collectors.toList());
    }
}
