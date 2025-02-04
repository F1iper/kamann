package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceCodes {
    INVALID_ATTENDANCE_STATE("INVALID_ATTENDANCE_STATE"),
    ATTENDANCE_NOT_FOUND("ATTENDANCE_NOT_FOUND"),
    ALREADY_REGISTERED("ALREADY_REGISTERED"),
    OCCURRENCE_EVENT_NOT_FOUND("OCCURRENCE_EVENT_NOT_FOUND");

    private final String code;
}
