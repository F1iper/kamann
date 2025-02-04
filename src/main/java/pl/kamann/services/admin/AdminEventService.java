package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.dmfs.rfc5545.recur.RecurrenceRule;
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
import pl.kamann.entities.event.EventFrequency;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.services.EventValidationService;
import pl.kamann.services.NotificationService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final OccurrenceEventRepository occurrenceRepository;
    private final EventMapper eventMapper;
    private final EventValidationService eventValidationService;
    private final NotificationService notificationService;
    private final EntityLookupService entityLookupService;
    private final PaginationService paginationService;

    @Transactional
    public EventDto createEvent(EventDto eventDto) {
        eventValidationService.validate(eventDto);

        var createdBy = entityLookupService.findUserById(eventDto.createdById());
        var eventType = entityLookupService.findEventTypeById(eventDto.eventTypeId());

        var event = eventMapper.toEntity(eventDto);
        event.setCreatedBy(createdBy);
        event.setEventType(eventType);

        eventRepository.save(event);

        LocalTime startTime = eventDto.startTime();
        LocalTime endTime = eventDto.endTime();

        generateOccurrencesForEvent(event, startTime, endTime);

        return eventMapper.toDto(event);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto eventDto) {
        eventValidationService.validate(eventDto);
        var existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        var eventType = entityLookupService.findEventTypeById(eventDto.eventTypeId());

        eventMapper.updateEventFromDto(existingEvent, eventDto);

        existingEvent.setEventType(eventType);

        existingEvent.setInstructor(entityLookupService.findUserById(eventDto.instructorId()));
        existingEvent.setStartTime(eventDto.startTime());
        existingEvent.setEndTime(eventDto.endTime());

        occurrenceRepository.deleteByEvent(existingEvent);

        generateOccurrencesForEvent(existingEvent, eventDto.startTime(), eventDto.endTime());

        return eventMapper.toDto(eventRepository.save(existingEvent));
    }

    public void deleteEvent(Long id, boolean force) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));

        if (!force && occurrenceRepository.existsByEvent(event)) {
            throw new ApiException(
                    "Cannot delete event with occurrences unless forced",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.EVENT_HAS_OCCURRENCES.name()
            );
        }

        occurrenceRepository.deleteByEvent(event);

        eventRepository.delete(event);
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

        List<EventDto> eventDtos = events.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());

        PaginationMetaData metaData = new PaginationMetaData(

                events.getTotalPages(),
                events.getTotalElements()
                );

        return new PaginatedResponseDto<>(eventDtos, metaData);
    }

    public PaginatedResponseDto<EventDto> listEventsByInstructor(Long instructorId, Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);

        Page<OccurrenceEvent> occurrencePage = occurrenceRepository.findDistinctByInstructorId(instructorId, validatedPageable);

        List<EventDto> eventDtos = occurrencePage.stream()
                .map(occurrence -> eventMapper.toDto(occurrence.getEvent()))
                .collect(Collectors.toList());

        PaginationMetaData metaData = new PaginationMetaData(
                occurrencePage.getTotalPages(),
                occurrencePage.getTotalElements()
        );

        return new PaginatedResponseDto<>(eventDtos, metaData);
    }

    public EventDto getEventById(Long eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND, EventCodes.EVENT_NOT_FOUND.name()
                ));
        return eventMapper.toDto(event);
    }

    @Transactional
    public void cancelEvent(Long id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Event not found", HttpStatus.NOT_FOUND, EventCodes.EVENT_NOT_FOUND.name()
                ));

        // Cancel future occurrences
        List<OccurrenceEvent> futureOccurrences = occurrenceRepository.findByEventAndDateAfter(event, LocalDate.now());
        futureOccurrences.forEach(occurrence -> occurrence.setCanceled(true));
        occurrenceRepository.saveAll(futureOccurrences);

        notificationService.notifyParticipants(event);
    }

    private void generateOccurrencesForEvent(Event event, LocalTime startTime, LocalTime endTime) {
        if (!event.getRecurring()) {
            OccurrenceEvent occurrence = OccurrenceEvent.builder()
                    .event(event)
                    .date(event.getStartDate())
                    .startTime(startTime)
                    .endTime(endTime)
                    .instructor(event.getInstructor())
                    .canceled(false)
                    .seriesIndex(1)
                    .build();

            occurrenceRepository.save(occurrence);
            return;
        }

        if (event.getRecurrenceRule() != null) {
            // Extract days of the week and frequency from the recurrence rule
            String daysOfWeek = extractDaysOfWeek(event.getRecurrenceRule());
            EventFrequency frequency = extractFrequency(event.getRecurrenceRule());
            LocalDate endDate = event.getRecurrenceEndDate();

            // Generate the event dates
            List<LocalDate> dates = generateEventDates(
                    event.getStartDate(),
                    daysOfWeek,
                    frequency,
                    endDate
            );

            // Map the dates to OccurrenceEvent objects
            List<OccurrenceEvent> occurrences = dates.stream()
                    .map(date -> OccurrenceEvent.builder()
                            .event(event)
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .instructor(event.getInstructor())
                            .canceled(false)
                            .seriesIndex(getSeriesIndex(date, event))
                            .build())
                    .collect(Collectors.toList());

            // Save all occurrences to the repository
            occurrenceRepository.saveAll(occurrences);
        } else {
            throw new ApiException("Invalid recurrence rule for event", HttpStatus.BAD_REQUEST, EventCodes.INVALID_RECURRENCE_RULE.name());
        }

    }


    private int getSeriesIndex(LocalDate date, Event event) {
        // Assuming the recurrence start date is the event's startDate
        return (int) ChronoUnit.WEEKS.between(event.getStartDate(), date) + 1;
    }


    private List<LocalDate> generateEventDates(LocalDate startDate, String daysOfWeek, EventFrequency frequency, LocalDate endDate) {
        List<LocalDate> generatedDates = new ArrayList<>();

        // Convert the comma-separated list of days of the week to a Set
        Set<DayOfWeek> daysOfWeekSet = Arrays.stream(daysOfWeek.split(","))
                .map(String::trim)
                .map(this::parseDayOfWeek)
                .collect(Collectors.toSet());

        LocalDate currentDate = startDate;

        // Based on the frequency, increment the currentDate differently
        switch (frequency) {
            case DAILY:
                // For daily recurrence, just add every day
                while (!currentDate.isAfter(endDate)) {
                    if (daysOfWeekSet.contains(currentDate.getDayOfWeek())) {
                        generatedDates.add(currentDate);
                    }
                    currentDate = currentDate.plusDays(1);
                }
                break;

            case WEEKLY:
                // For weekly recurrence, increment the date by one week
                while (!currentDate.isAfter(endDate)) {
                    if (daysOfWeekSet.contains(currentDate.getDayOfWeek())) {
                        generatedDates.add(currentDate);
                    }
                    currentDate = currentDate.plusWeeks(1);
                }
                break;

            case MONTHLY:
                // For monthly recurrence, increment the date by one month
                while (!currentDate.isAfter(endDate)) {
                    if (daysOfWeekSet.contains(currentDate.getDayOfWeek())) {
                        generatedDates.add(currentDate);
                    }
                    currentDate = currentDate.plusMonths(1);
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported frequency: " + frequency);
        }

        return generatedDates;
    }

    private DayOfWeek parseDayOfWeek(String day) {
        return switch (day.toLowerCase()) {
            case "monday" -> DayOfWeek.MONDAY;
            case "tuesday" -> DayOfWeek.TUESDAY;
            case "wednesday" -> DayOfWeek.WEDNESDAY;
            case "thursday" -> DayOfWeek.THURSDAY;
            case "friday" -> DayOfWeek.FRIDAY;
            case "saturday" -> DayOfWeek.SATURDAY;
            case "sunday" -> DayOfWeek.SUNDAY;
            default ->
                    throw new ApiException("Invalid day of week: " + day,
                            HttpStatus.BAD_REQUEST,
                            EventCodes.INVALID_DAY_OF_THE_WEEK.name());
        };
    }

    private String extractDaysOfWeek(RecurrenceRule recurrenceRule) {
        StringBuilder daysOfWeek = new StringBuilder();

        List<RecurrenceRule.WeekdayNum> byDays = recurrenceRule.getByDayPart();

        if (byDays != null && !byDays.isEmpty()) {
            Map<String, String> dayMap = Map.of(
                    "MO", "Monday",
                    "TU", "Tuesday",
                    "WE", "Wednesday",
                    "TH", "Thursday",
                    "FR", "Friday",
                    "SA", "Saturday",
                    "SU", "Sunday"
            );

            for (RecurrenceRule.WeekdayNum day : byDays) {
                if (!daysOfWeek.isEmpty()) {
                    daysOfWeek.append(", ");
                }
                String weekdayShort = day.weekday.name();
                daysOfWeek.append(dayMap.getOrDefault(weekdayShort, weekdayShort));
            }
        }

        return daysOfWeek.toString();
    }

    private EventFrequency extractFrequency(RecurrenceRule recurrenceRule) {
        String freq = recurrenceRule.getFreq().name();

        return switch (freq) {
            case "DAILY" -> EventFrequency.DAILY;
            case "WEEKLY" -> EventFrequency.WEEKLY;
            case "MONTHLY" -> EventFrequency.MONTHLY;
            case "YEARLY" -> EventFrequency.YEARLY;
            default -> throw new ApiException(
                    "Unsupported frequency: " + freq,
                    HttpStatus.BAD_REQUEST,
                    EventCodes.INVALID_FREQUENCY.name());
        };
    }
}
