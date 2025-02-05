package pl.kamann.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OccurrenceEventMapper {

    public OccurrenceEventDto toDto(OccurrenceEvent occurrenceEvent) {
        if (occurrenceEvent == null) {
            throw new IllegalArgumentException("OccurrenceEvent must not be null");
        }

        return OccurrenceEventDto.builder()
                .eventId(getEventId(occurrenceEvent))
                .date(occurrenceEvent.getStart().toLocalDate())
                .startTime(occurrenceEvent.getStart().toLocalTime())
                .endTime(occurrenceEvent.getEnd().toLocalTime())
                .canceled(occurrenceEvent.isCanceled())
                .seriesIndex(occurrenceEvent.getSeriesIndex())
                .instructorId(getInstructorId(occurrenceEvent))
                .instructorFullName(getInstructorFullName(occurrenceEvent))
                .createdById(getCreatedById(occurrenceEvent))
                .maxParticipants(getMaxParticipants(occurrenceEvent))
                .eventTypeName(getEventTypeName(occurrenceEvent))
                .isModified(isModified(occurrenceEvent))
                .attendanceCount(getAttendanceCount(occurrenceEvent))
                .build();
    }

    public OccurrenceEventDto toOccurrenceEventDto(OccurrenceEvent occurrence) {
        if (occurrence == null) {
            throw new IllegalArgumentException("OccurrenceEvent cannot be null");
        }

        Event event = occurrence.getEvent();
        LocalDateTime start = occurrence.getStart();
        AppUser instructor = occurrence.getInstructor();

        return OccurrenceEventDto.builder()
                .eventId(event != null ? event.getId() : null)
                .date(start.toLocalDate())
                .startTime(start.toLocalTime())
                .endTime(occurrence.getEnd().toLocalTime())
                .durationMinutes(occurrence.getDurationMinutes())
                .canceled(occurrence.isCanceled())
                .instructorId(instructor != null ? instructor.getId() : null)
                .createdById(occurrence.getCreatedBy() != null ? occurrence.getCreatedBy().getId() : null)
                .seriesIndex(occurrence.getSeriesIndex())
                .maxParticipants(occurrence.getMaxParticipants())
                .eventTypeName(event != null && event.getEventType() != null ? event.getEventType().getName() : null)
                .instructorFullName(instructor != null ?
                        instructor.getFirstName() + " " + instructor.getLastName() : null)
                .isModified(occurrence.isModified())
                .attendanceCount(occurrence.getAttendances() != null ?
                        occurrence.getAttendances().size() : 0)
                .build();
    }

    private Long getEventId(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getEvent())
                .map(Event::getId)
                .orElseThrow(() -> new IllegalStateException("OccurrenceEvent must have associated Event"));
    }

    private Long getInstructorId(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getInstructor())
                .map(AppUser::getId)
                .orElse(null);
    }

    private String getInstructorFullName(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getInstructor())
                .map(instructor -> instructor.getFirstName() + " " + instructor.getLastName())
                .orElse("No instructor assigned");
    }

    private Long getCreatedById(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getCreatedBy())
                .map(AppUser::getId)
                .orElse(null);
    }

    private Integer getMaxParticipants(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getEvent())
                .map(Event::getMaxParticipants)
                .orElse(0);
    }

    private String getEventTypeName(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getEvent())
                .flatMap(event -> Optional.ofNullable(event.getEventType()))
                .map(EventType::getName)
                .orElse("No type specified");
    }

    private boolean isModified(OccurrenceEvent occurrenceEvent) {
        Event parentEvent = occurrenceEvent.getEvent();
        return parentEvent == null ||
                !occurrenceEvent.getStart().toLocalDate().equals(parentEvent.getStart().toLocalDate()) ||
                !occurrenceEvent.getEnd().toLocalDate().equals(parentEvent.getEnd().toLocalDate()) ||
                occurrenceEvent.isCanceled() ||
                !Objects.equals(occurrenceEvent.getInstructor(), parentEvent.getInstructor());
    }

    private Integer getAttendanceCount(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getAttendances())
                .map(List::size)
                .orElse(0);
    }
}
