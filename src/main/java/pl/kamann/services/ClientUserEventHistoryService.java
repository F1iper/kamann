package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.ClientEventHistory;
import pl.kamann.repositories.UserEventHistoryRepository;
import pl.kamann.entities.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientUserEventHistoryService {

    private final UserEventHistoryRepository userEventHistoryRepository;
    private final EntityLookupService lookupService;

    public List<ClientEventHistory> getEventHistoryByUser(Long userId) {
        AppUser user = lookupService.getLoggedInUser();
        if (!user.getId().equals(userId)) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }
        return userEventHistoryRepository.findByUser(user);
    }

    public List<ClientEventHistory> getEventHistoryByEvent(Long eventId) {
        throw new UnsupportedOperationException("Clients cannot fetch event-wide history.");
    }
}
