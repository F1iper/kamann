package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.AttendanceReportDto;
import pl.kamann.dtos.AttendanceStat;

@Component
public class AttendanceReportMapper {

    public AttendanceReportDto toDto(AttendanceStat stat) {
        return new AttendanceReportDto(
                stat.getEventName(),
                stat.getTotalParticipants(),
                stat.getAttended(),
                stat.getAbsent(),
                stat.getLateCancellations()
        );
    }
}
