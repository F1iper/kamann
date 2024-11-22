package pl.kamann.attendance.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.attendance.handler.AttendanceHandler;
import pl.kamann.attendance.service.client.ClientAttendanceService;
import pl.kamann.attendance.service.management.ManagementAttendanceService;

@Service
@RequiredArgsConstructor
public class AttendanceServiceFactory {

    private final ClientAttendanceService clientAttendanceService;
    private final ManagementAttendanceService managementAttendanceService;

    public AttendanceHandler getHandler(String role) {
        if ("CLIENT".equalsIgnoreCase(role)) {
            return clientAttendanceService;
        } else if ("INSTRUCTOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return managementAttendanceService;
        }
        throw new IllegalArgumentException("Unsupported role: " + role);
    }
}
