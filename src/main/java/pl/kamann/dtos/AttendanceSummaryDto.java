package pl.kamann.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceSummaryDto {
    private long totalAttendance;
    private long presentCount;
    private long lateCancelCount;
    private long earlyCancelCount;
}