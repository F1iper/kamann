package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventFrequency;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.Recurrence;

import java.util.ArrayList;

@Component
public class EventMapper {

    public EventDto toDto(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        Recurrence recurrence = event.getRecurrence();

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
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .currentParticipants(event.getCurrentParticipants())
                .eventTypeId(event.getEventType() != null ? event.getEventType().getId() : null)
                .eventTypeName(event.getEventType() != null ? event.getEventType().getName() : null)

                .recurrence_frequency(recurrence != null ? recurrence.getFrequency() : null)
                .recurrence_daysOfWeek(recurrence != null ?
                        new ArrayList<>(recurrence.getDaysOfWeek()) : null)
                .recurrence_EndDate(recurrence != null ? recurrence.getRecurrenceEndDate() : null)
                .build();
    }

    public Event toEntity(EventDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("EventDto cannot be null");
        }

        Event event = Event.builder()
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
                .build();

        if (dto.recurring()) {
            Recurrence recurrence = Recurrence.builder()
                    .frequency(dto.recurrence_frequency() != null ?
                            EventFrequency.valueOf(dto.recurrence_frequency().name()) : null)
                    .daysOfWeek(dto.recurrence_daysOfWeek() != null ?
                            new ArrayList<>(dto.recurrence_daysOfWeek()) : null)
                    .recurrenceEndDate(dto.recurrence_EndDate())
                    .build();
            event.setRecurrence(recurrence);
        }

        return event;
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

        if (updatedEventDto.recurring()) {
            Recurrence recurrence = Recurrence.builder()
                    .frequency(updatedEventDto.recurrence_frequency() != null ?
                            EventFrequency.valueOf(updatedEventDto.recurrence_frequency().name()) : null)
                    .daysOfWeek(updatedEventDto.recurrence_daysOfWeek() != null ?
                            new ArrayList<>(updatedEventDto.recurrence_daysOfWeek()) : null)
                    .recurrenceEndDate(updatedEventDto.recurrence_EndDate())
                    .build();
            existingEvent.setRecurrence(recurrence);
        } else {
            existingEvent.setRecurrence(null);
        }
    }
}