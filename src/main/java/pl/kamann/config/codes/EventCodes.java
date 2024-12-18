package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventCodes {
    EVENT_NOT_FOUND("EVENT_NOT_FOUND"),
    PAST_EVENT_ERROR("PAST_EVENT_ERROR"),
    EVENT_TYPE_NOT_FOUND("EVENT_TYPE_NOT_FOUND"),
    EVENT_FULL("EVENT_FULL"),
    INVALID_EVENT_TIME("INVALID_EVENT_TIME"),
    CANNOT_CANCEL_STARTED_EVENT("CANNOT_CANCEL_STARTED_EVENT"),
    EVENT_HAS_PARTICIPANTS("EVENT_HAS_PARTICIPANTS");

    private final String code;
}
