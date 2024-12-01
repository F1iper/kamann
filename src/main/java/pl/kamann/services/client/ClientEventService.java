package pl.kamann.services.client;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.EventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.EventStatus;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.AttendanceRepository;
import pl.kamann.repositories.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final EventMapper eventMapper;
    private final ClientEventHistoryService clientEventHistoryService;

    public List<EventDto> getAvailableEvents(AppUser client) {
        validateClient(client);

        return eventRepository.findAvailableEventsExcludingClient(LocalDateTime.now(), client.getId())
                .stream()
                .map(eventMapper::toDto)
                .toList();
    }


    public List<EventDto> getRegisteredEvents(AppUser user) {
        if (user == null) {
            throw new ApiException("User cannot be null", HttpStatus.BAD_REQUEST, Codes.INVALID_REQUEST);
        }

        var registeredEvents = eventRepository.findEventsByUserAndStatus(user, AttendanceStatus.REGISTERED);
        return registeredEvents.stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public List<EventDto> getAllEvents(AppUser user) {
        if (user == null) {
            throw new ApiException("User cannot be null", HttpStatus.BAD_REQUEST, Codes.INVALID_REQUEST);
        }

        var allEvents = eventRepository.findAllEventsByUser(user);
        return allEvents.stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public EventDto getEventDetails(Long eventId) {
        var event = findEventById(eventId);
        return eventMapper.toDto(event);
    }

    @Transactional
    public void finalizeEvent(Long eventId) {
        var event = findEventById(eventId);

        var attendances = attendanceRepository.findUnmarkedOrRegisteredAttendancesByEvent(event);

        attendances.forEach(attendance -> {
            attendance.setStatus(AttendanceStatus.ABSENT);
            attendanceRepository.save(attendance);

            try {
                clientEventHistoryService.logEventHistory(
                        attendance.getUser(),
                        event,
                        AttendanceStatus.ABSENT
                );
            } catch (Exception e) {
                // todo: handle the exception
                System.err.println("Failed to log event history for user: " + attendance.getUser().getId());
                e.printStackTrace();
            }
        });

        event.setStatus(EventStatus.COMPLETED);
        eventRepository.save(event);
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        Codes.EVENT_NOT_FOUND));
    }

    private void validateClient(AppUser client) {
        if (client == null) {
            throw new ApiException("Client cannot be null.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_REQUEST);
        }
    }
}
