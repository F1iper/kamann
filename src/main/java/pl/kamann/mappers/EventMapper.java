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
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .time(event.getTime())
                .recurring(event.isRecurring())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .instructorId(event.getInstructor() != null ? event.getInstructor().getId() : null)
                .instructorFullName(getInstructorFullName(event))
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .currentParticipants(event.getCurrentParticipants())
                .eventTypeId(event.getEventType() != null ? event.getEventType().getId() : null)
                .eventTypeName(event.getEventType() != null ? event.getEventType().getName() : null)
                .frequency(event.getFrequency())
                .daysOfWeek(event.getDaysOfWeek())
                .recurrenceEndDate(event.getRecurrenceEndDate())
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
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .time(dto.time())
                .recurring(dto.recurring())
                .maxParticipants(dto.maxParticipants())
                .status(dto.status() != null ? dto.status() : EventStatus.SCHEDULED)
                .currentParticipants(dto.currentParticipants())
                .frequency(dto.frequency())
                .daysOfWeek(dto.daysOfWeek())
                .recurrenceEndDate(dto.recurrenceEndDate())
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
        if (updatedEventDto.startDate() != null) {
            existingEvent.setStartDate(updatedEventDto.startDate());
        }
        if (updatedEventDto.endDate() != null) {
            existingEvent.setEndDate(updatedEventDto.endDate());
        }
        if (updatedEventDto.time() != null) {
            existingEvent.setTime(updatedEventDto.time());
        }

        existingEvent.setRecurring(updatedEventDto.recurring());
        existingEvent.setMaxParticipants(updatedEventDto.maxParticipants());

        if (updatedEventDto.status() != null) {
            existingEvent.setStatus(updatedEventDto.status());
        }

        existingEvent.setFrequency(updatedEventDto.frequency());
        existingEvent.setDaysOfWeek(updatedEventDto.daysOfWeek() != null ?
                (updatedEventDto.daysOfWeek()) : null);
        existingEvent.setRecurrenceEndDate(updatedEventDto.recurrenceEndDate());
    }

    private String getInstructorFullName(Event event) {
        return event.getInstructor() != null
                ? event.getInstructor().getFirstName() + " " + event.getInstructor().getLastName()
                : null;
    }
}
