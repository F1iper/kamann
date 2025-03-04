package pl.kamann.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kamann.dtos.AdminMembershipCardRequest;
import pl.kamann.dtos.MembershipCardResponse;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface MembershipCardMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "membershipCardType", source = "membershipCardType.displayName")
    MembershipCardResponse toMembershipCardResponse(MembershipCard membershipCard);

    @Mapping(target = "membershipCardType", source = "adminMembershipCardRequest.membershipCardType")
    @Mapping(target = "user", source = "appUser")
    @Mapping(target = "startDate", expression = "java(mapStartDate(adminMembershipCardRequest))")
    @Mapping(target = "endDate", expression = "java(mapEndDate(adminMembershipCardRequest))")
    MembershipCard toMembershipCard(AdminMembershipCardRequest adminMembershipCardRequest, AppUser appUser);

    default LocalDateTime mapStartDate(AdminMembershipCardRequest adminMembershipCardRequest) {
        return adminMembershipCardRequest.startDate() != null ? adminMembershipCardRequest.startDate() : LocalDateTime.now();
    }

    default  LocalDateTime mapEndDate(AdminMembershipCardRequest adminMembershipCardRequest) {
        return adminMembershipCardRequest.endDate() != null ? adminMembershipCardRequest.endDate() : LocalDateTime.now().plusDays(MembershipCardType.valueOf(adminMembershipCardRequest.membershipCardType()).getValidDays());
    }
}
