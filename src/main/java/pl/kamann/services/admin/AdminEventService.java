package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.event.EventDto;
import pl.kamann.dtos.event.EventUpdateRequest;
import pl.kamann.dtos.event.EventUpdateResponse;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.dtos.event.CreateEventResponse;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.entities.event.EventType;
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
import pl.kamann.utility.PaginationUtil;

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
    private final EventTypeService eventTypeService;
    private final EventValidationService eventValidationService;

    private final OccurrenceEventRepository occurrenceEventRepository;
    private final OccurrenceEventMapper occurrenceEventMapper;

    private final NotificationService notificationService;
    private final EntityLookupService entityLookupService;
    private final PaginationService paginationService;
    private final PaginationUtil paginationUtil;

    private final EntityLookupService lookupService;

    @Transactional
    public CreateEventResponse createEvent(CreateEventRequest request) {
        eventValidationService.validateCreate(request);

        Event event = eventMapper.toEvent(request, lookupService);

        event.setCreatedBy(entityLookupService.findUserById(request.createdById()));

        EventType eventType = eventTypeService.findOrCreateEventType(request.eventTypeName());
        event.setEventType(eventType);

        event = eventRepository.save(event);

        occurrenceEventRepository.saveAll(generateOccurrences(event));

        return eventMapper.toCreateEventResponse(event);
    }

    public PaginatedResponseDto<EventDto> listEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").ascending());
        pageable = paginationService.validatePageable(pageable);

        Page<Event> pagedEvents = eventRepository.findAll(pageable);

        return paginationUtil.toPaginatedResponse(pagedEvents, eventMapper::toEventDto);
    }

    @Transactional
    public EventUpdateResponse updateEvent(Long id, EventUpdateRequest requestDto) {
        Event event = entityLookupService.findEventById(id);

        eventValidationService.validateUpdate(requestDto, event);

        updateEventFields(event, requestDto);
        eventRepository.save(event);

        return eventMapper.toEventUpdateResponse(event);
    }

    @Transactional
    public void deleteEvent(Long id, boolean force) {
        Event event = entityLookupService.findEventById(id);

        if (!force && occurrenceEventRepository.existsByEvent(event)) {
            throw new ApiException("Cannot delete event with occurrences unless forced",
                    HttpStatus.BAD_REQUEST, EventCodes.EVENT_HAS_OCCURRENCES.name());
        }

        occurrenceEventRepository.deleteByEvent(event);
        eventRepository.delete(event);
    }

    @Transactional
    public void cancelEvent(Long id, EventStatus eventStatus) {
        Event event = entityLookupService.findEventById(id);
        LocalDateTime now = LocalDateTime.now();

        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ApiException("Event is already canceled.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.EVENT_ALREADY_CANCELED.name());
        }

        event.setStatus(eventStatus);
        event.setUpdatedAt(LocalDateTime.now());

        List<OccurrenceEvent> futureOccurrences = occurrenceEventRepository.findByEventAndStartAfter(event, now);
        futureOccurrences.forEach(occ -> {
            occ.setCanceled(true);
            occ.setEventStatus(eventStatus);
        });

        eventRepository.save(event);
        occurrenceEventRepository.saveAll(futureOccurrences);

        notificationService.notifyParticipants(event);
    }

    private void updateEventFields(Event event, EventUpdateRequest requestDto) {
        event.setTitle(requestDto.title());
        event.setDescription(requestDto.description());
        event.setStart(requestDto.start());
        event.setDurationMinutes(requestDto.durationMinutes());
        event.setRrule(requestDto.rrule());
        event.setMaxParticipants(requestDto.maxParticipants());
        event.setInstructor(requestDto.instructorId() != null ? entityLookupService.findUserById(requestDto.instructorId()) : null);
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

            // todo: At the moment we are using a limit to avoid infinite loops if the RRULE lacks an UNTIL or COUNT
            //  system variable might be used
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

    public EventDto getEventDtoById(Long eventId) {
        Event eventById = entityLookupService.findEventById(eventId);

        return eventMapper.toEventDto(eventById);
    }

    public OccurrenceEventDto getOccurrenceById(Long occurrenceId) {
        OccurrenceEvent occurrenceEvent = occurrenceEventRepository.getReferenceById(occurrenceId);

        return occurrenceEventMapper.toOccurrenceEventDto(occurrenceEvent);
    }
}