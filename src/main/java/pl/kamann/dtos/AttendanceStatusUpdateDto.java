package pl.kamann.dtos;

import lombok.Getter;
import lombok.Setter;
import pl.kamann.entities.attendance.AttendanceStatus;

@Getter
@Setter
public class AttendanceStatusUpdateDto {
    private Long eventId;
    private Long clientId;
    private AttendanceStatus status;
}