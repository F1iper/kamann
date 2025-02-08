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
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.config.pagination.PaginationMetaData;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.EventResponseDto;
import pl.kamann.dtos.EventUpdateRequestDto;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventType;
import pl.kamann.entities.event.EventUpdateScope;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.services.EventTypeService;
import pl.kamann.services.EventValidationService;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final OccurrenceEventRepository occurrenceEventRepository;
    private final OccurrenceEventMapper occurrenceEventMapper;

    private final EventTypeService eventTypeService;
    private final EventValidationService eventValidationService;
    private final NotificationService notificationService;
    private final EntityLookupService entityLookupService;
    private final PaginationService paginationService;

    @Transactional
    public CreateEventResponse createEvent(CreateEventRequest request) {
        eventValidationService.validate(request);

        Event event = eventMapper.toEntity(request);

        event.setCreatedBy(entityLookupService.findUserById(request.createdById()));

        EventType eventType = eventTypeService.findOrCreateEventType(request.eventTypeName());
        event.setEventType(eventType);

        event = eventRepository.save(event);

        occurrenceEventRepository.saveAll(generateOccurrences(event));

        return eventMapper.toCreateEventResponse(event);
    }

    @Transactional
    public EventResponseDto updateEvent(Long id, EventUpdateRequestDto requestDto, EventUpdateScope scope) {
        Event event = findEventById(id);
        updateEventFields(event, requestDto);
        eventRepository.save(event);
        updateOccurrences(event, scope);
        return eventMapper.toResponseDto(event);
    }

    @Transactional
    public void deleteEvent(Long id, boolean force) {
        Event event = findEventById(id);

        if (!force && occurrenceEventRepository.existsByEvent(event)) {
            throw new ApiException("Cannot delete event with occurrences unless forced",
                    HttpStatus.BAD_REQUEST, EventCodes.EVENT_HAS_OCCURRENCES.name());
        }

        occurrenceEventRepository.deleteByEvent(event);
        eventRepository.delete(event);
    }

    @Transactional
    public void cancelEvent(Long id) {
        Event event = findEventById(id);
        LocalDateTime now = LocalDateTime.now();

        List<OccurrenceEvent> futureOccurrences = occurrenceEventRepository.findByEventAndStartAfter(event, now);
        futureOccurrences.forEach(occ -> occ.setCanceled(true));

        occurrenceEventRepository.saveAll(futureOccurrences);
        notificationService.notifyParticipants(event);
    }

    public PaginatedResponseDto<EventDto> listAllEvents(Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);
        Page<Event> events = eventRepository.findAll(validatedPageable);
        return new PaginatedResponseDto<>(
                events.map(eventMapper::toDto).getContent(),
                new PaginationMetaData(events.getTotalPages(), events.getTotalElements())
        );
    }

    private void updateEventFields(Event event, EventUpdateRequestDto requestDto) {
        event.setTitle(requestDto.title());
        event.setDescription(requestDto.description());
        event.setStart(requestDto.start());
        event.setDurationMinutes(requestDto.durationMinutes());
        event.setRrule(requestDto.rrule());
        event.setMaxParticipants(requestDto.maxParticipants());
        event.setInstructor(requestDto.instructorId() != null ? entityLookupService.findUserById(requestDto.instructorId()) : null);
    }

    private void updateOccurrences(Event event, EventUpdateScope scope) {
        if (scope == EventUpdateScope.EVENT_ONLY) return;

        LocalDateTime now = LocalDateTime.now();
        List<OccurrenceEvent> occurrences = occurrenceEventRepository.findAllByEventId(event.getId());

        occurrences.stream()
                .filter(occ -> scope == EventUpdateScope.ALL_OCCURRENCES || occ.getStart().isAfter(now))
                .forEach(occ -> updateOccurrenceFields(occ, event));

        occurrenceEventRepository.saveAll(occurrences);
    }

    private void updateOccurrenceFields(OccurrenceEvent occ, Event event) {
        occ.setDurationMinutes(event.getDurationMinutes());
        occ.setMaxParticipants(event.getMaxParticipants());
        occ.setInstructor(event.getInstructor());
    }

    public List<OccurrenceEvent> generateOccurrences(Event event) {
        List<OccurrenceEvent> occurrences = new ArrayList<>();

        // If no RRULE is provided, create a single occurrence (one-time event)
        if (event.getRrule() == null || event.getRrule().isEmpty()) {
            occurrences.add(createOccurrence(event, event.getStart(), 0));
            return occurrences;
        }

        try {
            RecurrenceRule rule = new RecurrenceRule(event.getRrule());
            DateTime dtStart = new DateTime(
                    event.getStart().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            RecurrenceRuleIterator iterator = rule.iterator(dtStart);

            // Use a limit to avoid infinite loops if the RRULE lacks an UNTIL or COUNT
            int maxInstances = 25;
            int seriesIndex = 1;
            while (iterator.hasNext() && maxInstances-- > 0) {
                DateTime nextDateTime = iterator.nextDateTime();
                LocalDateTime occurrenceStart = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(nextDateTime.getTimestamp()),
                        ZoneId.systemDefault()
                );
                occurrences.add(createOccurrence(event, occurrenceStart, seriesIndex++));
            }
        } catch (Exception e) {
            throw new ApiException(
                    "Failed to generate occurrences: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    EventCodes.OCCURRENCE_GENERATION_FAILED.name());
        }

        return occurrences;
    }


    private OccurrenceEvent createOccurrence(Event event, LocalDateTime start, int seriesIndex) {
        return OccurrenceEvent.builder()
                .event(event)
                .start(start)
                .createdBy(event.getCreatedBy())
                .durationMinutes(event.getDurationMinutes())
                .maxParticipants(event.getMaxParticipants())
                .instructor(event.getInstructor())
                .seriesIndex(seriesIndex)
                .build();
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ApiException("Event not found with ID: " + id,
                        HttpStatus.NOT_FOUND, EventCodes.EVENT_NOT_FOUND.name()));
    }


    public PaginatedResponseDto<EventDto> listEventsByInstructor(Long instructorId, Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);
        Page<Event> eventPage = eventRepository.findAllByInstructorId(instructorId, validatedPageable);

        return new PaginatedResponseDto<>(
                eventPage.map(eventMapper::toDto).getContent(),
                new PaginationMetaData(eventPage.getTotalPages(), eventPage.getTotalElements())
        );
    }

    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        return eventMapper.toDto(event);
    }

    public OccurrenceEventDto getOccurrenceById(Long occurrenceId) {
        OccurrenceEvent occurrenceEvent = occurrenceEventRepository.getReferenceById(occurrenceId);

        return occurrenceEventMapper.toOccurrenceEventDto(occurrenceEvent);
    }
}