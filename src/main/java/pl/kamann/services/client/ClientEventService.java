package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventDto;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.attendance.AttendanceStatus;
import pl.kamann.entities.event.Event;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.EventMapper;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final EventRepository eventRepository;
    private final OccurrenceEventMapper occurrenceEventMapper;
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final EntityLookupService lookupService;

    public List<OccurrenceEventDto> getAvailableEvents() {
        AppUser loggedInUser = lookupService.getLoggedInUser();
        validateClient(loggedInUser);

        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        List<OccurrenceEvent> availableEvents = occurrenceEventRepository.findAvailableEventsExcludingClient(nowDate, nowTime, loggedInUser.getId());

        return availableEvents.stream()
                .map(occurrenceEventMapper::toDto)
                .toList();
    }


    public List<OccurrenceEventDto> getRegisteredEvents(AppUser user) {
        if (user == null) {
            throw new ApiException(
                    "User cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_REQUEST.name()
            );
        }

        var registeredEvents = eventRepository.findEventsByUserAndStatus(user, AttendanceStatus.REGISTERED);
        return registeredEvents.stream()
                .map(occurrenceEventMapper::toDto)
                .toList();
    }

    public OccurrenceEventDto getEventDetails(Long eventId) {
        var event = findEventById(eventId);
        return occurrenceEventMapper.toDto(event);
    }

    private OccurrenceEvent findEventById(Long eventId) {
        return occurrenceEventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with ID: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }

    private void validateClient(AppUser client) {
        if (client == null) {
            throw new ApiException("Client cannot be null.",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_REQUEST.name()
            );
        }
    }
}
