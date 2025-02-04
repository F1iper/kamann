package pl.kamann.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventMapper {

    public EventDto toDto(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .recurring(event.getRecurring())
                .rrule(event.getRrule())
                .exdates(Optional.ofNullable(event.getExdates()).orElseGet(Collections::emptyList))
                .maxParticipants(event.getOccurrenceLimit())
                .recurrenceEndDate(event.getRecurrenceEndDate())
                .createdById(Optional.ofNullable(event.getCreatedBy()).map(AppUser::getId).orElse(null))
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .eventTypeId(Optional.ofNullable(event.getEventType()).map(EventType::getId).orElse(null))
                .eventTypeName(Optional.ofNullable(event.getEventType()).map(EventType::getName).orElse(null))
                .instructorId(Optional.ofNullable(event.getInstructor()).map(AppUser::getId).orElse(null))
                .startDate(event.getStartDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
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
                .recurring(dto.recurring())
                .rrule(dto.rrule())
                .exdates(Optional.ofNullable(dto.exdates()).orElseGet(Collections::emptyList))
                .occurrenceLimit(dto.maxParticipants())
                .recurrenceEndDate(dto.recurrenceEndDate())
                .status(dto.status() != null ? dto.status() : EventStatus.SCHEDULED)
                .maxParticipants(dto.maxParticipants())
                .startDate(dto.startDate())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .build();
    }

    public void updateEventFromDto(Event existingEvent, EventDto updatedEventDto) {
        if (existingEvent == null || updatedEventDto == null) {
            throw new IllegalArgumentException("ExistingEvent and UpdatedEventDto cannot be null");
        }

        Optional.ofNullable(updatedEventDto.title()).ifPresent(existingEvent::setTitle);
        Optional.ofNullable(updatedEventDto.description()).ifPresent(existingEvent::setDescription);
        Optional.ofNullable(updatedEventDto.exdates()).ifPresent(existingEvent::setExdates);
        Optional.of(updatedEventDto.maxParticipants()).ifPresent(existingEvent::setOccurrenceLimit);
        Optional.ofNullable(updatedEventDto.recurrenceEndDate()).ifPresent(existingEvent::setRecurrenceEndDate);
        Optional.ofNullable(updatedEventDto.status()).ifPresent(existingEvent::setStatus);

        if (updatedEventDto.recurring() != null) {
            existingEvent.setRecurring(updatedEventDto.recurring());
        }
        existingEvent.setMaxParticipants(updatedEventDto.maxParticipants());
        if (updatedEventDto.rrule() != null) {
            existingEvent.setRrule(updatedEventDto.rrule());
        }
        if (updatedEventDto.startDate() != null) {
            existingEvent.setStartDate(updatedEventDto.startDate());
        }
        if (updatedEventDto.startTime() != null) {
            existingEvent.setStartTime(updatedEventDto.startTime());
        }
        if (updatedEventDto.endTime() != null) {
            existingEvent.setEndTime(updatedEventDto.endTime());
        }
    }

}
