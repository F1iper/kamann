package pl.kamann.systemevents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.OccurrenceEvent;

@Getter
@AllArgsConstructor
public class EventHistoryLogEvent {
    private final AppUser user;
    private final OccurrenceEvent event;
    private final AttendanceStatus status;
}
