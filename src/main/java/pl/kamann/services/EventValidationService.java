package pl.kamann.services;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.RecurrenceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;

import java.time.LocalDateTime;

@Service
public class EventValidationService {

    public void validate(EventDto eventDto) {
        validateBasicFields(eventDto);
        validateRrule(eventDto);
    }

    private void validateBasicFields(EventDto eventDto) {
        if (eventDto.start() == null) {
            throw new ApiException(
                    "Start date/time is required",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_DATE_REQUIRED.name()
            );
        }

        if (eventDto.durationMinutes() == null || eventDto.durationMinutes() <= 0) {
            throw new ApiException(
                    "Duration must be positive",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.INVALID_DURATION.name()
            );
        }

        if (eventDto.id() == null && eventDto.start().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Event start cannot be in the past",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_IN_PAST.name()
            );
        }
    }

    private void validateRrule(EventDto eventDto) {
        String rrule = eventDto.rrule();
        if (rrule == null || rrule.isBlank()) {
            return;
        }

        try {
            RecurrenceRule rule = new RecurrenceRule(rrule);
            if (rule.getUntil() != null) {
                DateTime untilDateTime = rule.getUntil();
                if (untilDateTime.getTimestamp() < eventDto.start().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()) {
                    throw new ApiException(
                            "RRULE UNTIL date cannot be before start date",
                            HttpStatus.BAD_REQUEST,
                            RecurrenceCodes.INVALID_UNTIL_DATE.name()
                    );
                }
            }
        } catch (InvalidRecurrenceRuleException e) {
            throw new ApiException(
                    "Invalid RRULE format: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.INVALID_RECURRENCE_RULE.name()
            );
        }
    }
}
