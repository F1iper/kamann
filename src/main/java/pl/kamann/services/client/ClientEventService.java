package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.*;
import pl.kamann.dtos.event.EventDto;
import pl.kamann.dtos.event.EventLightDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;
import pl.kamann.utility.PaginationUtil;

@Service
@RequiredArgsConstructor
public class ClientEventService {
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final OccurrenceEventMapper occurrenceEventMapper;

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final EntityLookupService lookupService;
    private final PaginationService paginationService;
    private final PaginationUtil paginationUtil;

    public PaginatedResponseDto<OccurrenceEventLightDto> getOccurrences(OccurrenceEventScope scope, int page, int size) {
        if (scope == null) {
            scope = OccurrenceEventScope.UPCOMING;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("start").ascending());
        pageable = paginationService.validatePageable(pageable);

        AppUser loggedInUser = lookupService.getLoggedInUser();

        Page<OccurrenceEvent> pagedOccurrences = occurrenceEventRepository.findFilteredOccurrences(
                scope.name(), loggedInUser, pageable);

        return paginationUtil.toPaginatedResponse(pagedOccurrences, occurrenceEventMapper::toOccurrenceEventLightDto);
    }

    public PaginatedResponseDto<EventLightDto> getLightEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").ascending());
        pageable = paginationService.validatePageable(pageable);
        Page<Event> pagedEvents = eventRepository.findAll(pageable);

        return paginationUtil.toPaginatedResponse(pagedEvents, eventMapper::toEventLightDto);
    }

    public PaginatedResponseDto<EventDto> getEventsByType(String eventType, int page, int size) {
        String capitalizedEventType = eventType.substring(0, 1).toUpperCase() + eventType.substring(1).toLowerCase();
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").ascending());
        pageable = paginationService.validatePageable(pageable);

        Page<Event> pagedEvent = eventRepository.findAllByEventTypeName(capitalizedEventType, pageable);
        return paginationUtil.toPaginatedResponse(pagedEvent, eventMapper::toEventDto);
    }

    public OccurrenceEventDto getOccurrenceById(Long occurrenceId) {
        OccurrenceEvent occurrenceEvent = occurrenceEventRepository.findById(occurrenceId)
                .orElseThrow(() -> new ApiException(
                        "OccurrenceEvent not found with ID: " + occurrenceId,
                        HttpStatus.BAD_REQUEST,
                        EventCodes.OCCURRENCE_NOT_FOUND.name()));

        return occurrenceEventMapper.toOccurrenceEventDto(occurrenceEvent);
    }

    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.BAD_REQUEST,
                        EventCodes.EVENT_NOT_FOUND.name()));

        return eventMapper.toEventDto(event);
    }
}