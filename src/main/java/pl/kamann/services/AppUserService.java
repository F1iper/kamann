package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.config.pagination.PaginationMetaData;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.utility.EntityLookupService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final EntityLookupService entityLookupService;
    private final RoleRepository roleRepository;

    public PaginatedResponseDto<AppUserDto> getUsers(int page, int size, String roleName) {
        int defaultPage = 1;
        int defaultSize = 10;

        page = (page > 0) ? page : defaultPage;
        size = (size > 0) ? size : defaultSize;

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AppUser> pagedUsers;

        if (roleName == null || roleName.isEmpty()) {
            pagedUsers = appUserRepository.findAllWithRoles(pageable);
        } else {
            Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new ApiException(
                            "Role not found: " + roleName,
                            HttpStatus.NOT_FOUND,
                            StatusCodes.NO_RESULTS.name()));

            pagedUsers = appUserRepository.findUsersByRoleWithRoles(pageable, role);
        }

        List<AppUserDto> userDtos = pagedUsers.getContent().stream()
                .map(appUser -> new AppUserDto(
                        appUser.getId(),
                        appUser.getEmail(),
                        appUser.getFirstName(),
                        appUser.getLastName(),
                        appUser.getRoles(),
                        appUser.getStatus()
                ))
                .toList();

        PaginationMetaData metaData = new PaginationMetaData(
                pagedUsers.getTotalPages(),
                pagedUsers.getTotalElements()
        );

        return new PaginatedResponseDto<>(userDtos, metaData);
    }



    public AppUserDto getUserById(Long id) {
        if (id == null) {
            throw new ApiException(
                    "User ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        AppUser user = entityLookupService.findUserById(id);
        return appUserMapper.toDto(user);
    }

    public AppUserDto createUser(AppUserDto userDto) {
        if (userDto.email() == null || userDto.email().isBlank()) {
            throw new ApiException(
                    "Email cannot be null or blank",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        if (userDto.roles() == null || userDto.roles().isEmpty()) {
            throw new ApiException(
                    "Roles cannot be null or empty",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        entityLookupService.validateEmailNotTaken(userDto.email());

        Set<Role> roles = entityLookupService.findRolesByNameIn(userDto.roles());

        AppUser user = appUserMapper.toEntity(userDto, roles);

        if (user.getStatus() == null) {
            user.setStatus(AppUserStatus.ACTIVE);
        }

        return appUserMapper.toDto(appUserRepository.save(user));
    }

    public AppUserDto changeUserStatus(Long userId, AppUserStatus status) {
        if (userId == null) {
            throw new ApiException(
                    "User ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        if (status == null) {
            throw new ApiException(
                    "Status cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        AppUser user = entityLookupService.findUserById(userId);
        user.setStatus(status);
        return appUserMapper.toDto(appUserRepository.save(user));
    }

    public void activateUser(Long userId) {
        changeUserStatus(userId, AppUserStatus.ACTIVE);
    }

    public void deactivateUser(Long userId) {
        changeUserStatus(userId, AppUserStatus.INACTIVE);
    }

    public Page<AppUserDto> getUsersByRole(String roleName, Pageable pageable) {
        if (roleName == null || roleName.isBlank()) {
            throw new ApiException(
                    "Role name cannot be null or blank",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.NOT_FOUND,
                        AuthCodes.ROLE_NOT_FOUND.name()
                ));

        Page<AppUser> users = appUserRepository.findByRolesContaining(role, pageable);

        if (users.isEmpty()) {
            return Page.empty(pageable);
        }

        return appUserMapper.toDtoPage(users);
    }
}