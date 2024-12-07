package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.entities.event.Event;
import pl.kamann.dtos.UserEventHistoryDto;
import pl.kamann.entities.event.ClientEventHistory;
import pl.kamann.entities.appuser.AppUser;

@Component
public class UserEventHistoryMapper {

    public UserEventHistoryDto toDto(ClientEventHistory history) {
        return UserEventHistoryDto.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .eventId(history.getEvent().getId())
                .status(history.getStatus())
                .attendedDate(history.getAttendedDate())
                .entrancesUsed(history.getEntrancesUsed())
                .build();
    }

    public ClientEventHistory toEntity(UserEventHistoryDto dto, AppUser user, Event event) {
        ClientEventHistory history = new ClientEventHistory();
        history.setId(dto.id());
        history.setUser(user);
        history.setEvent(event);
        history.setStatus(dto.status());
        history.setAttendedDate(dto.attendedDate());
        history.setEntrancesUsed(dto.entrancesUsed());
        return history;
    }
}
