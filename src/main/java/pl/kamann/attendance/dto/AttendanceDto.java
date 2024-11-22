package pl.kamann.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kamann.attendance.model.AttendanceStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {

    private Long id;
    private Long userId;
    private Long eventId;
    private AttendanceStatus status;
    private LocalDateTime timestamp;
}