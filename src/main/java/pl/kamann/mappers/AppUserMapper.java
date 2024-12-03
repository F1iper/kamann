package pl.kamann.mappers;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AppUserMapper {

    public AppUserDto toDto(AppUser user) {
        if (user == null) {
            return null;
        }

        return AppUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .status(user.getStatus())
                .build();
    }

    public AppUser toEntity(AppUserDto dto, Set<Role> roles) {
        if (dto == null) {
            return null;
        }

        return AppUser.builder()
                .id(dto.id())
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .roles(roles)
                .status(dto.status()).build();

    }

    public List<AppUserDto> toDtoList(List<AppUser> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}