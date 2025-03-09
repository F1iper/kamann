package pl.kamann.mappers;

import org.mapstruct.Mapper;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.AppUserResponseDto;
import pl.kamann.entities.appuser.AppUser;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    AppUserDto toAppUserDto(AppUser user);

    PaginatedResponseDto<AppUserDto> toPaginatedResponseDto(PaginatedResponseDto<AppUser> users);

    AppUserResponseDto toAppUserResponseDto(AppUser loggedInUser);
}