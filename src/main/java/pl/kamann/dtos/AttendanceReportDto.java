package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportDto {
    private String eventName;
    private long totalParticipants;
    private long attended;
    private long absent;
    private long lateCancellations;
}
