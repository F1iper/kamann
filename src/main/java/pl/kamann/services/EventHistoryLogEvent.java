package pl.kamann.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;

@Getter
@AllArgsConstructor
public class EventHistoryLogEvent {
    private final AppUser user;
    private final Event event;
    private final AttendanceStatus status;
}
