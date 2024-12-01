package pl.kamann.services.client;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.entities.*;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.UserEventRegistrationRepository;
import pl.kamann.utility.EntityLookupService;

@Service
@RequiredArgsConstructor
public class ClientEventRegistrationService {

    private final AttendanceRepository attendanceRepository;
    private final EntityLookupService lookupService;
    private final ClientEventHistoryService clientEventHistoryService;
    private final UserEventRegistrationRepository registrationRepository;

    @Transactional
    public void registerUserForEvent(Long eventId, Long clientId) {
        var client = lookupService.findUserById(clientId);
        var event = lookupService.findEventById(eventId);

        if (registrationRepository.existsByUserAndEvent(client, event)) {
            throw new ApiException(
                    "User is already registered.",
                    HttpStatus.CONFLICT,
                    Codes.ALREADY_REGISTERED);
        }

        int registeredCount = registrationRepository.countByEventAndStatus(event, UserEventRegistrationStatus.REGISTERED);

        UserEventRegistration registration = new UserEventRegistration();
        registration.setUser(client);
        registration.setEvent(event);

        if (registeredCount >= event.getMaxParticipants()) {
            int waitlistCount = registrationRepository.countByEventAndWaitlistPositionIsNotNull(event);
            registration.setStatus(UserEventRegistrationStatus.WAITLISTED);
            registration.setWaitlistPosition(waitlistCount + 1);
        } else {
            registration.setStatus(UserEventRegistrationStatus.REGISTERED);
        }

        registrationRepository.save(registration);

        AttendanceStatus historyStatus = registration.getStatus() == UserEventRegistrationStatus.WAITLISTED
                ? AttendanceStatus.WAITLISTED
                : AttendanceStatus.REGISTERED;
        clientEventHistoryService.logEventHistory(client, event, historyStatus);
    }

    @Transactional
    public void cancelUserRegistration(Long eventId, Long clientId) {
        var attendance = attendanceRepository.findByUserAndEvent(
                        lookupService.findUserById(clientId), lookupService.findEventById(eventId))
                .orElseThrow(() -> new ApiException(
                        "Attendance not found.",
                        HttpStatus.NOT_FOUND,
                        Codes.ATTENDANCE_NOT_FOUND));

        attendance.setStatus(AttendanceStatus.CANCELED_BY_CLIENT);
        attendanceRepository.save(attendance);

        clientEventHistoryService.logEventHistory(attendance.getUser(), attendance.getEvent(), AttendanceStatus.CANCELED_BY_CLIENT);
    }

    @Transactional
    public void handleWaitlist(Long eventId) {
        var event = lookupService.findEventById(eventId);

        var waitlistedRegistration = registrationRepository.findFirstByEventAndStatusOrderByWaitlistPositionAsc(
                event, UserEventRegistrationStatus.WAITLISTED);

        if (waitlistedRegistration == null) {
            return;
        }

        waitlistedRegistration.setStatus(UserEventRegistrationStatus.REGISTERED);
        waitlistedRegistration.setWaitlistPosition(null);
        registrationRepository.save(waitlistedRegistration);

        clientEventHistoryService.logEventHistory(
                waitlistedRegistration.getUser(),
                event,
                AttendanceStatus.REGISTERED
        );
    }

}
