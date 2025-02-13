package pl.kamann.mapper;

import org.junit.jupiter.api.Test;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    @Test
    void toDto_shouldMapEventCorrectly() {
        AppUser creator = AppUser.builder().id(1L).build();
        AppUser instructor = AppUser.builder().id(2L).firstName("John").lastName("Doe").build();
        EventType type = EventType.builder().id(1L).name("Yoga").build();
        OccurrenceEvent occ1 = OccurrenceEvent.builder().participants(Collections.singletonList(creator)).build();
        OccurrenceEvent occ2 = OccurrenceEvent.builder().participants(Collections.singletonList(instructor)).build();

        Event event = Event.builder()
                .id(100L)
                .title("Morning Yoga")
                .description("A relaxing session")
                .start(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .rrule("FREQ=DAILY")
                .status(EventStatus.SCHEDULED)
                .createdBy(creator)
                .eventType(type)
                .maxParticipants(20)
                .instructor(instructor)
                .occurrences(Arrays.asList(occ1, occ2))
                .build();

        EventMapper mapper = new EventMapper();
        EventDto dto = mapper.toDto(event);

        assertEquals(event.getId(), dto.id());
        assertEquals(event.getTitle(), dto.title());
        assertEquals(event.getDescription(), dto.description());
        assertEquals(event.getStart(), dto.start());
        assertEquals(event.getDurationMinutes(), dto.durationMinutes());
        assertEquals(event.getRrule(), dto.rrule());
        assertEquals(creator.getId(), dto.createdById());
        assertEquals(instructor.getId(), dto.instructorId());
        assertEquals("John Doe", dto.instructorFullName());
        assertEquals(event.getMaxParticipants(), dto.maxParticipants());
        assertEquals(EventStatus.SCHEDULED, dto.status());
        int expectedParticipants = occ1.getParticipants().size() + occ2.getParticipants().size();
        assertEquals(expectedParticipants, dto.currentParticipants());
        assertEquals(type.getId(), dto.eventTypeId());
        assertEquals(type.getName(), dto.eventTypeName());
    }
}
