package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.UserCardHistoryDto;
import pl.kamann.entities.ClientMembershipCardHistory;
import pl.kamann.entities.AppUser;

@Component
public class UserCardHistoryMapper {

    public UserCardHistoryDto toDto(ClientMembershipCardHistory history) {
        return UserCardHistoryDto.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .membershipCardType(history.getMembershipCardType())
                .startDate(history.getStartDate())
                .endDate(history.getEndDate())
                .entrances(history.getEntrances())
                .remainingEntrances(history.getRemainingEntrances())
                .paid(history.isPaid())
                .build();
    }

    public ClientMembershipCardHistory toEntity(UserCardHistoryDto dto, AppUser user) {
        ClientMembershipCardHistory history = new ClientMembershipCardHistory();
        history.setId(dto.getId());
        history.setUser(user);
        history.setMembershipCardType(dto.getMembershipCardType());
        history.setStartDate(dto.getStartDate());
        history.setEndDate(dto.getEndDate());
        history.setEntrances(dto.getEntrances());
        history.setRemainingEntrances(dto.getRemainingEntrances());
        history.setPaid(dto.isPaid());
        return history;
    }
}