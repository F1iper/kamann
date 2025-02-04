package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.OccurrenceEventDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.repositories.OccurrenceEventRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientOccurrenceService {

    private final OccurrenceEventRepository occurrenceRepository;
    private final OccurrenceEventMapper occurrenceMapper;
    private final EntityLookupService lookupService;

    public List<OccurrenceEventDto> getAvailableOccurrences() {
        AppUser loggedInUser = lookupService.getLoggedInUser();

        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        List<OccurrenceEvent> availableOccurrences =
                occurrenceRepository.findAvailableEventsExcludingClient(nowDate, nowTime, loggedInUser.getId());

        return availableOccurrences.stream()
                .map(occurrenceMapper::toDto)
                .toList();
    }

    public List<OccurrenceEventDto> getRegisteredOccurrences(AppUser user) {
        if (user == null) {
            throw new ApiException(
                    "User cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_REQUEST.name()
            );
        }

        List<OccurrenceEvent> registeredOccurrences =
                occurrenceRepository.findRegisteredOccurrencesByUser(user.getId());

        return registeredOccurrences.stream()
                .map(occurrenceMapper::toDto)
                .toList();
    }

    public OccurrenceEventDto getOccurrenceDetails(Long occurrenceId) {
        OccurrenceEvent occurrence = occurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new ApiException(
                        "Occurrence not found with ID: " + occurrenceId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
        return occurrenceMapper.toDto(occurrence);
    }
}
