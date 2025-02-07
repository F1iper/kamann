package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;

@Component
public class OccurrenceEventMapper {

    public OccurrenceEventDto toOccurrenceEventDto(OccurrenceEvent occ) {
        return new OccurrenceEventDto(
                occ.getEvent().getId(),
                occ.getStart().toLocalDate(),
                occ.getStart().toLocalTime(),
                occ.getEnd().toLocalTime(),
                occ.getDurationMinutes(),
                occ.isCanceled(),
                occ.getInstructor() != null ? occ.getInstructor().getId() : null,
                occ.getCreatedBy() != null ? occ.getCreatedBy().getId() : null,
                occ.getSeriesIndex(),
                occ.getMaxParticipants(),
                occ.getEvent().getEventType() != null ? occ.getEvent().getEventType().getName() : null,
                occ.getInstructor() != null ? occ.getInstructor().getFirstName() + " " + occ.getInstructor().getLastName() : null,
                occ.isModified(),
                occ.getAttendances() != null ? occ.getAttendances().size() : 0
        );
    }

    public OccurrenceEventLightDto toLightDto(OccurrenceEvent event) {
        String instructorName = event.getInstructor() != null ? event.getInstructor().getFirstName() + " " + event.getInstructor().getLastName() : null;
        LocalDateTime end = event.getStart().plusMinutes(event.getDurationMinutes());
        return new OccurrenceEventLightDto(event.getId(), event.getEvent().getId(), event.getStart(), end, event.getEvent().getTitle(), instructorName);
    }

}