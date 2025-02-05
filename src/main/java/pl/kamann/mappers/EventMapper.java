package pl.kamann.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventMapper {

    public EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .start(event.getStart())
                .durationMinutes(event.getDurationMinutes())
                .rrule(event.getRrule())
                .createdById(event.getCreatedBy().getId())
                .instructorId(Optional.ofNullable(event.getInstructor()).map(AppUser::getId).orElse(null))
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .eventTypeId(event.getEventType().getId())
                .eventTypeName(event.getEventType().getName())
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

    public void updateEntityFromDto(Event event, EventDto dto) {
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setStart(dto.start());
        event.setDurationMinutes(dto.durationMinutes());
        event.setRrule(dto.rrule());
        event.setStatus(dto.status());
        event.setMaxParticipants(dto.maxParticipants());
    }
}
