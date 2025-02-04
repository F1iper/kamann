package pl.kamann.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;

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
                .id(occurrenceEvent.getId())
                .eventId(getEventId(occurrenceEvent))
                .date(occurrenceEvent.getDate())
                .startTime(occurrenceEvent.getStartTime())
                .endTime(occurrenceEvent.getEndTime())
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
                !occurrenceEvent.getStartTime().equals(parentEvent.getStartTime()) ||
                !occurrenceEvent.getEndTime().equals(parentEvent.getEndTime()) ||
                occurrenceEvent.isCanceled() ||
                !Objects.equals(occurrenceEvent.getInstructor(), parentEvent.getInstructor());
    }

    private Integer getAttendanceCount(OccurrenceEvent occurrenceEvent) {
        return Optional.ofNullable(occurrenceEvent.getAttendances())
                .map(List::size)
                .orElse(0);
    }
}
