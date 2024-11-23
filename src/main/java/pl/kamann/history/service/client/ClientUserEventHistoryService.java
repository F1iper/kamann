package pl.kamann.history.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.history.handler.EventHistoryHandler;
import pl.kamann.history.model.ClientEventHistory;
import pl.kamann.history.repository.UserEventHistoryRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientUserEventHistoryService implements EventHistoryHandler {

    private final UserEventHistoryRepository userEventHistoryRepository;
    private final EntityLookupService lookupService;

    @Override
    public List<ClientEventHistory> getEventHistoryByUser(Long userId) {
        AppUser user = lookupService.getLoggedInUser();
        if (!user.getId().equals(userId)) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }
        return userEventHistoryRepository.findByUser(user);
    }

    @Override
    public List<ClientEventHistory> getEventHistoryByEvent(Long eventId) {
        throw new UnsupportedOperationException("Clients cannot fetch event-wide history.");
    }
}
