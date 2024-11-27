package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.entities.Event;
import pl.kamann.dtos.UserEventHistoryDto;
import pl.kamann.entities.ClientEventHistory;
import pl.kamann.entities.AppUser;

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
        history.setId(dto.getId());
        history.setUser(user);
        history.setEvent(event);
        history.setStatus(dto.getStatus());
        history.setAttendedDate(dto.getAttendedDate());
        history.setEntrancesUsed(dto.getEntrancesUsed());
        return history;
    }
}
