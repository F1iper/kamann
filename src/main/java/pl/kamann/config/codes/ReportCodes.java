package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportCodes {
    NO_EVENT_STATS("NO_EVENT_STATS"),
    NO_REVENUE_STATS("NO_REVENUE_STATS"),
    NO_ATTENDANCE_STATS("NO_ATTENDANCE_STATS");

    private final String code;
}
