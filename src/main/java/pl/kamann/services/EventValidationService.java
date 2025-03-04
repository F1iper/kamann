package pl.kamann.services;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.codes.RecurrenceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.event.EventUpdateRequest;
import pl.kamann.dtos.event.CreateEventRequest;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;

import java.time.LocalDateTime;

@Service
public class EventValidationService {

    public void validateCreate(CreateEventRequest request) {
        validateRequiredFields(request.start(), request.durationMinutes());
        validateTitle(request.title());
        validateDescription(request.description());
        validateStart(request.start(), null);
        validateRrule(request.rrule(), request.start());
        validateMaxParticipants(request.maxParticipants());
    }

    public void validateUpdate(EventUpdateRequest requestDto, Event event) {
        validateStatus(event);
        validateTitle(requestDto.title());
        validateDescription(requestDto.description());
        validateStart(requestDto.start(), event.getStart());
        validateDuration(requestDto.durationMinutes());
        validateMaxParticipants(requestDto.maxParticipants());
    }

    private static void validateStatus(Event event) {
        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ApiException("Cannot update a cancelled event.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.EVENT_ALREADY_CANCELLED.name());
        }
    }

    private void validateTitle(String title) {
        if (StringUtils.hasText(title) && title.length() > 255) {
            throw new ApiException("Title cannot exceed 255 characters.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.INVALID_EVENT_TITLE.name());
        }
    }

    private void validateDescription(String description) {
        if (StringUtils.hasText(description) && description.length() > 1000) {
            throw new ApiException("Description cannot exceed 1000 characters.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.INVALID_EVENT_DESCRIPTION.name());
        }
    }

    private void validateRequiredFields(LocalDateTime start, Integer durationMinutes) {
        if (start == null) {
            throw new ApiException("Start date/time is required.",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_DATE_REQUIRED.name());
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new ApiException("Duration must be positive.",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.INVALID_DURATION.name());
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new ApiException("Event start cannot be in the past.",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_IN_PAST.name());
        }
    }

    private void validateStart(LocalDateTime newStart, LocalDateTime originalStart) {
        if (newStart != null) {
            if (newStart.isBefore(LocalDateTime.now())) {
                throw new ApiException("Start time cannot be in the past.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_EVENT_START.name());
            }
            if (originalStart != null && newStart.isBefore(originalStart)) {
                throw new ApiException("Updated start time cannot be earlier than the original start.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_EVENT_START_UPDATE.name());
            }
        }
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes != null && durationMinutes <= 0) {
            throw new ApiException("Duration must be a positive number.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.INVALID_EVENT_DURATION.name());
        }
    }

    private void validateMaxParticipants(Integer maxParticipants) {
        if (maxParticipants != null && maxParticipants <= 0) {
            throw new ApiException("Max participants must be greater than zero.",
                    HttpStatus.BAD_REQUEST,
                    EventCodes.INVALID_MAX_PARTICIPANTS.name());
        }
    }

    private void validateRrule(String rrule, LocalDateTime start) {
        if (rrule == null || rrule.isBlank()) {
            return; // RRULE is optional, so return early if not provided
        }

        try {
            RecurrenceRule rule = new RecurrenceRule(rrule);
            if (rule.getUntil() != null) {
                DateTime untilDateTime = rule.getUntil();
                if (untilDateTime.getTimestamp() < start.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()) {
                    throw new ApiException("RRULE UNTIL date cannot be before start date.",
                            HttpStatus.BAD_REQUEST,
                            RecurrenceCodes.INVALID_UNTIL_DATE.name());
                }

                LocalDateTime untilLocalDate = LocalDateTime.ofEpochSecond(
                        untilDateTime.getTimestamp() / 1000, 0, java.time.ZoneOffset.UTC);
                LocalDateTime maxAllowedUntil = start.plusMonths(2);
                if (untilLocalDate.isAfter(maxAllowedUntil)) {
                    throw new ApiException("RRULE UNTIL date cannot be more than 2 months after the event start.",
                            HttpStatus.BAD_REQUEST,
                            RecurrenceCodes.INVALID_UNTIL_DATE.name());
                }
            }
        } catch (InvalidRecurrenceRuleException e) {
            throw new ApiException("Invalid RRULE format: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.INVALID_RECURRENCE_RULE.name());
        }
    }
}