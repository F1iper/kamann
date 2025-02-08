package pl.kamann.services;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.RecurrenceCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.event.CreateEventRequest;

import java.time.LocalDateTime;

@Service
public class EventValidationService {

    public void validate(CreateEventRequest request) {
        validateBasicFields(request);
        validateRrule(request);
    }

    private void validateBasicFields(CreateEventRequest request) {
        if (request.start() == null) {
            throw new ApiException(
                    "Start date/time is required",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_DATE_REQUIRED.name()
            );
        }

        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new ApiException(
                    "Duration must be positive",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.INVALID_DURATION.name()
            );
        }

        if (request.start().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "Event start cannot be in the past",
                    HttpStatus.BAD_REQUEST,
                    RecurrenceCodes.START_IN_PAST.name()
            );
        }
    }

    private void validateRrule(CreateEventRequest request) {
        String rrule = request.rrule();
        if (rrule == null || rrule.isBlank()) {
            return;
        }

        try {
            RecurrenceRule rule = new RecurrenceRule(rrule);
            if (rule.getUntil() != null) {
                DateTime untilDateTime = rule.getUntil();

                if (untilDateTime.getTimestamp() < request.start()
                        .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()) {
                    throw new ApiException(
                            "RRULE UNTIL date cannot be before start date",
                            HttpStatus.BAD_REQUEST,
                            RecurrenceCodes.INVALID_UNTIL_DATE.name()
                    );
                }

                LocalDateTime untilLocalDate = LocalDateTime.ofEpochSecond(
                        untilDateTime.getTimestamp() / 1000, 0, java.time.ZoneOffset.UTC);

                // Enforce a maximum allowed UNTIL date: 2 months after the event start.
                LocalDateTime maxAllowedUntil = request.start().plusMonths(2);
                if (untilLocalDate.isAfter(maxAllowedUntil)) {
                    throw new ApiException(
                            "RRULE UNTIL date cannot be more than 2 months after the event start.",
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
