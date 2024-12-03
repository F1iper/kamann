package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceStat {
    private String eventName;
    private long totalParticipants;
    private long attended;
    private long absent;
    private long lateCancellations;
}
