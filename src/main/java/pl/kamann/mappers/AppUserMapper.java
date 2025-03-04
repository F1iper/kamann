package pl.kamann.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.AppUserResponseDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Role;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface AppUserMapper {
    AppUserDto toAppUserDto(AppUser user);

    @Mapping(target = "roles", source = "roles")
    AppUser toAppUser(AppUserDto dto, Set<Role> roles);

    PaginatedResponseDto<AppUserDto> toPaginatedResponseDto(PaginatedResponseDto<AppUser> users);

    AppUserResponseDto toAppUserResponseDto(AppUser loggedInUser);

}
