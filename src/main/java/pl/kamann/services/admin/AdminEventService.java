package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.config.pagination.PaginationMetaData;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.services.EventValidationService;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventService {
    private final EventRepository eventRepository;
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final EventMapper eventMapper;
    private final EventValidationService eventValidationService;
    private final NotificationService notificationService;
    private final EntityLookupService entityLookupService;
    private final PaginationService paginationService;

    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        eventValidationService.validate(eventDto);

        Event event = eventMapper.toEntity(eventDto);
        event.setCreatedBy(entityLookupService.findUserById(eventDto.createdById()));
        event.setEventType(entityLookupService.findEventTypeById(eventDto.eventTypeId()));

        Event savedEvent = eventRepository.save(event);
        generateOccurrences(savedEvent);

        return eventMapper.toDto(savedEvent);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto eventDto) {
        Event existingEvent = findEventById(eventId);
        boolean rruleChanged = !Objects.equals(existingEvent.getRrule(), eventDto.rrule());

        updateEventFields(existingEvent, eventDto);
        Event savedEvent = eventRepository.save(existingEvent);

        if (rruleChanged) {
            regenerateOccurrences(savedEvent);
        }

        return eventMapper.toDto(savedEvent);
    }

    private void updateEventFields(Event event, EventDto dto) {
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setStart(dto.start());
        event.setDurationMinutes(dto.durationMinutes());
        event.setRrule(dto.rrule());
        event.setMaxParticipants(dto.maxParticipants());

        if (dto.instructorId() != null) {
            event.setInstructor(entityLookupService.findUserById(dto.instructorId()));
        }

        if (dto.eventTypeId() != null) {
            event.setEventType(entityLookupService.findEventTypeById(dto.eventTypeId()));
        }
    }

    @Transactional
    public void deleteEvent(Long id, boolean force) {
        Event event = findEventById(id);

        if (!force && occurrenceEventRepository.existsByEvent(event)) {
            throw new ApiException(
                    "Cannot delete event with occurrences unless forced",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.EVENT_HAS_OCCURRENCES.name()
            );
        }

        occurrenceEventRepository.deleteByEvent(event);
        eventRepository.delete(event);
    }

    @Transactional
    public void cancelEvent(Long id) {
        Event event = findEventById(id);
        LocalDateTime now = LocalDateTime.now();

        List<OccurrenceEvent> futureOccurrences =
                occurrenceEventRepository.findByEventAndStartAfter(event, now);

        futureOccurrences.forEach(occurrence -> occurrence.setCanceled(true));
        occurrenceEventRepository.saveAll(futureOccurrences);

        notificationService.notifyParticipants(event);
    }

    public List<OccurrenceEvent> generateOccurrences(Event event) {
        List<OccurrenceEvent> occurrences = new ArrayList<>();

        if (event.getRrule() == null || event.getRrule().isEmpty()) {
            OccurrenceEvent singleOccurrence = OccurrenceEvent.builder()
                    .event(event)
                    .start(event.getStart())
                    .durationMinutes(event.getDurationMinutes())
                    .maxParticipants(event.getMaxParticipants())
                    .instructor(event.getInstructor())
                    .seriesIndex(0)
                    .build();
            occurrences.add(singleOccurrence);
            return occurrences;
        }

        try {
            RecurrenceRule rule = new RecurrenceRule(event.getRrule());
            DateTime start = new DateTime(
                    event.getStart().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            );

            RecurrenceRuleIterator iterator = rule.iterator(start);
            int seriesIndex = 0;
            int maxInstances = 25;

            while (iterator.hasNext() && maxInstances-- > 0) {
                DateTime nextDateTime = iterator.nextDateTime();
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(nextDateTime.getTimestamp()),
                        ZoneId.systemDefault()
                );

                OccurrenceEvent occurrence = OccurrenceEvent.builder()
                        .event(event)
                        .start(dateTime)
                        .durationMinutes(event.getDurationMinutes())
                        .maxParticipants(event.getMaxParticipants())
                        .instructor(event.getInstructor())
                        .seriesIndex(seriesIndex++)
                        .build();
                occurrences.add(occurrence);
            }

            return occurrences;
        } catch (Exception e) {
            throw new ApiException(
                    "Failed to generate occurrences: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    EventCodes.OCCURRENCE_GENERATION_FAILED.name()
            );
        }
    }

    private void regenerateOccurrences(Event event) {
        occurrenceEventRepository.deleteByEvent(event);
        generateOccurrences(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + id,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }

    public PaginatedResponseDto<EventDto> listAllEvents(Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);
        Page<Event> events = eventRepository.findAll(validatedPageable);

        if (events.isEmpty() && validatedPageable.getPageNumber() > 0) {
            throw new ApiException(
                    "No results for the requested page",
                    HttpStatus.NOT_FOUND,
                    StatusCodes.NO_RESULTS.name()
            );
        }

        List<EventDto> eventDtos = events.getContent().stream()
                .map(eventMapper::toDto)
                .toList();

        return new PaginatedResponseDto<>(
                eventDtos,
                new PaginationMetaData(events.getTotalPages(), events.getTotalElements())
        );
    }

    public PaginatedResponseDto<EventDto> listEventsByInstructor(Long instructorId, Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);
        Page<OccurrenceEvent> occurrencePage =
                occurrenceEventRepository.findDistinctByInstructorId(instructorId, validatedPageable);

        List<EventDto> eventDtos = occurrencePage.getContent().stream()
                .map(occurrence -> eventMapper.toDto(occurrence.getEvent()))
                .toList();

        return new PaginatedResponseDto<>(
                eventDtos,
                new PaginationMetaData(
                        occurrencePage.getTotalPages(),
                        occurrencePage.getTotalElements()
                )
        );
    }

    public EventDto getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(eventMapper::toDto)
                .orElseThrow(() -> new ApiException(
                        "Event not found with id: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()));
    }

    public void createSingleOccurrence(Event event) {
        createOccurrence(event, event.getStart(), 0);
    }

    private void createOccurrence(Event event, LocalDateTime start, int seriesIndex) {
        OccurrenceEvent occurrence = OccurrenceEvent.builder()
                .event(event)
                .start(start)
                .durationMinutes(event.getDurationMinutes())
                .maxParticipants(event.getMaxParticipants())
                .instructor(event.getInstructor())
                .createdBy(event.getCreatedBy())  // Set createdBy from Event
                .seriesIndex(seriesIndex)
                .build();
        occurrenceEventRepository.save(occurrence);
    }

    public void createRecurringOccurrences(Event event) {
        List<OccurrenceEvent> occurrences = generateOccurrences(event).stream()
                .map(occurrence -> OccurrenceEvent.builder()
                        .event(occurrence.getEvent())
                        .start(occurrence.getStart())
                        .durationMinutes(occurrence.getDurationMinutes())
                        .maxParticipants(occurrence.getMaxParticipants())
                        .instructor(occurrence.getInstructor())
                        .createdBy(event.getCreatedBy())
                        .seriesIndex(occurrence.getSeriesIndex())
                        .canceled(occurrence.isCanceled())
                        .excluded(occurrence.isExcluded())
                        .build())
                .collect(Collectors.toList());

        occurrenceEventRepository.saveAll(occurrences);
    }
}
