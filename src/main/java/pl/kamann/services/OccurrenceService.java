package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.Attendance;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OccurrenceService {
    private final EventRepository eventRepository;
    private final EntityLookupService lookupService;
    private final AttendanceRepository attendanceRepository;
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final OccurrenceEventMapper occurrenceEventMapper;

    private static final int MAX_OCCURRENCES = 50;

    public List<OccurrenceEventDto> getAvailableOccurrences() {
        List<Event> events = occurrenceEventRepository.findAllAvailableEvents();
        List<OccurrenceEventDto> availableOccurrences = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : events) {
            List<OccurrenceEvent> occurrences = getOccurrences(event);
            for (OccurrenceEvent occurrence : occurrences) {
                if (occurrence.getStart().isAfter(now)) {
                    availableOccurrences.add(occurrenceEventMapper.toOccurrenceEventDto(occurrence));
                }
            }
        }
        return availableOccurrences;
    }

    public List<OccurrenceEventDto> getUserEnrolledOccurrences() {
        AppUser currentUser = lookupService.getLoggedInUser();
        List<Attendance> attendances = attendanceRepository.findByUserAndOccurrenceEventStartAfter(
                currentUser,
                LocalDateTime.now()
        );

        return attendances.stream()
                .map(attendance -> occurrenceEventMapper.toOccurrenceEventDto(
                        attendance.getOccurrenceEvent()))
                .collect(Collectors.toList());
    }

    public OccurrenceEventDto getOccurrenceDetailsForUser(Long id) {
        AppUser currentUser = lookupService.getLoggedInUser();
        OccurrenceEvent occurrence = occurrenceEventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Occurrence not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.OCCURRENCE_NOT_FOUND.name()
                ));

        boolean hasAccess = occurrence.getParticipants().contains(currentUser) ||
                occurrence.getEvent().getStatus() == EventStatus.SCHEDULED;

        if (!hasAccess) {
            throw new ApiException(
                    "Access denied to occurrence details",
                    HttpStatus.FORBIDDEN,
                    AuthCodes.UNAUTHORIZED.name()
            );
        }

        return occurrenceEventMapper.toDto(occurrence);
    }

    public List<OccurrenceEvent> getOccurrences(Event event) {
        if (!event.isRecurring()) {
            return List.of(createOccurrence(event, event.getStart(), 0));
        }

        List<OccurrenceEvent> occurrences = new ArrayList<>();
        RecurrenceRule rule = event.getRecurrenceRule();
        LocalDateTime startDateTime = event.getStart();

        RecurrenceRuleIterator iterator = rule.iterator(new DateTime(
                startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ));

        int count = 0;
        while (iterator.hasNext() && count < MAX_OCCURRENCES) {
            DateTime nextDateTime = iterator.nextDateTime();
            LocalDateTime occurrenceDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(nextDateTime.getTimestamp()),
                    ZoneId.systemDefault()
            );

            occurrences.add(createOccurrence(event, occurrenceDateTime, count));
            count++;
        }

        return occurrences;
    }

    private OccurrenceEvent createOccurrence(Event event, LocalDateTime dateTime, int seriesIndex) {
        return OccurrenceEvent.builder()
                .event(event)
                .start(dateTime)
                .durationMinutes(event.getDurationMinutes())
                .maxParticipants(event.getMaxParticipants())
                .instructor(event.getInstructor())
                .seriesIndex(seriesIndex)
                .build();
    }

}
