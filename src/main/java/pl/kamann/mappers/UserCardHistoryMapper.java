package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.UserCardHistoryDto;
import pl.kamann.entities.membershipcard.ClientMembershipCardHistory;
import pl.kamann.entities.appuser.AppUser;

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
        history.setId(dto.id());
        history.setUser(user);
        history.setMembershipCardType(dto.membershipCardType());
        history.setStartDate(dto.startDate());
        history.setEndDate(dto.endDate());
        history.setEntrances(dto.entrances());
        history.setRemainingEntrances(dto.remainingEntrances());
        history.setPaid(dto.paid());
        return history;
    }
}