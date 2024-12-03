package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.reports.AttendanceReportDto;
import pl.kamann.entities.reports.AttendanceStatEntity;

@Component
public class AttendanceReportMapper {

    public AttendanceReportDto toDto(AttendanceStatEntity stat) {
        return new AttendanceReportDto(
                stat.getEventName(),
                stat.getTotalParticipants(),
                stat.getAttended(),
                stat.getAbsent(),
                stat.getLateCancellations()
        );
    }
}
