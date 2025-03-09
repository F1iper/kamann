package pl.kamann.mappers;

import org.mapstruct.Mapper;
import pl.kamann.dtos.AdminMembershipCardRequest;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface MembershipCardMapper {

    default LocalDateTime mapStartDate(AdminMembershipCardRequest adminMembershipCardRequest) {
        return adminMembershipCardRequest.startDate() != null ? adminMembershipCardRequest.startDate() : LocalDateTime.now();
    }

    default  LocalDateTime mapEndDate(AdminMembershipCardRequest adminMembershipCardRequest) {
        return adminMembershipCardRequest.endDate() != null ? adminMembershipCardRequest.endDate() : LocalDateTime.now().plusDays(MembershipCardType.valueOf(adminMembershipCardRequest.membershipCardType()).getValidDays());
    }
}
