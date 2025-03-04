package pl.kamann.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import pl.kamann.dtos.event.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.OccurrenceEvent;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventMapperTest {

    @InjectMocks
    private EventMapper mapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


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
                .eventType(type)
                .build();


        EventDto dto = mapper.toDto(event);

        assertEquals(event.getId(), dto.id());
        assertEquals(event.getTitle(), dto.title());
        assertEquals(event.getDescription(), dto.description());
        assertEquals(event.getStart(), dto.start());
        assertEquals(event.getDurationMinutes(), dto.durationMinutes());
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
