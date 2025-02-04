package pl.kamann.services;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.RecurrenceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class EventValidationService {

    public void validate(EventDto eventDto) {
        validateBasicFields(eventDto);
        validateRecurrenceRule(eventDto);
        validateTimeConsistency(eventDto);
    }

    private void validateBasicFields(EventDto eventDto) {
        if (eventDto.startDate() == null) {
            throw new ApiException(
                    "Start date is required",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_DATE_REQUIRED.name()
            );
        }

        if (eventDto.startTime() == null || eventDto.endTime() == null) {
            throw new ApiException(
                    "Both start and end times are required",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.TIME_REQUIRED.name()
            );
        }
    }

    private void validateRecurrenceRule(EventDto eventDto) {
        if (eventDto.recurring()) {
            if (eventDto.rrule() == null || eventDto.rrule().isBlank()) {
                throw new ApiException(
                        "RRULE is required for recurring events",
                        HttpStatus.BAD_REQUEST,
                        RecurrenceCodes.RECURRENCE_RULE_REQUIRED.name()
                );
            }

            try {
                RecurrenceRule rule = new RecurrenceRule(eventDto.rrule());
                validateRuleDates(eventDto, rule);
            } catch (InvalidRecurrenceRuleException e) {
                throw new ApiException(
                        "Invalid RRULE format: " + e.getMessage(),
                        HttpStatus.BAD_REQUEST,
                        RecurrenceCodes.INVALID_RECURRENCE_RULE.name()
                );
            }
        }
    }

    private void validateRuleDates(EventDto eventDto, RecurrenceRule rule) {
        if (rule.getUntil() != null) {
            DateTime untilDateTime = rule.getUntil();

            Instant instant = Instant.ofEpochMilli(untilDateTime.getTimestamp());
            LocalDate ruleUntil = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            if (ruleUntil.isBefore(eventDto.startDate())) {
                throw new ApiException(
                        "RRULE UNTIL date cannot be before start date",
                        HttpStatus.BAD_REQUEST,
                        RecurrenceCodes.INVALID_UNTIL_DATE.name()
                );
            }
        }
    }

    private void validateTimeConsistency(EventDto eventDto) {
        if (eventDto.endTime().isBefore(eventDto.startTime())) {
            throw new ApiException(
                    "End time must be after start time",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.INVALID_TIME_ORDER.name()
            );
        }

        if (eventDto.id() == null) { // New event
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime eventStart = LocalDateTime.of(eventDto.startDate(), eventDto.startTime());

            if (eventStart.isBefore(now)) {
                throw new ApiException(
                        "Event start cannot be in the past",
                        HttpStatus.BAD_REQUEST,
                        RecurrenceCodes.START_IN_PAST.name()
                );
            }
        }
    }
}
