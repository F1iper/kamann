package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
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

    public Page<AppUserDto> getAllUsers(Pageable pageable) {
        Page<AppUser> users = appUserRepository.findAll(pageable);
        if (users.isEmpty()) {
            throw new ApiException(
                    "No users found",
                    HttpStatus.NOT_FOUND,
                    Codes.USER_NOT_FOUND);
        }
        return users.map(appUserMapper::toDto);
    }

    public AppUserDto getUserById(Long id) {
        if (id == null) {
            throw new ApiException(
                    "User ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT);
        }

        AppUser user = entityLookupService.findUserById(id);
        return appUserMapper.toDto(user);
    }

    public AppUserDto createUser(AppUserDto userDto) {
        if (userDto.email() == null || userDto.email().isBlank()) {
            throw new ApiException(
                    "Email cannot be null or blank",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT);
        }

        if (userDto.roles() == null || userDto.roles().isEmpty()) {
            throw new ApiException(
                    "Roles cannot be null or empty",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT);
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
                    Codes.INVALID_INPUT);
        }

        if (status == null) {
            throw new ApiException(
                    "Status cannot be null",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT);
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
                    Codes.INVALID_INPUT
            );
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.NOT_FOUND,
                        Codes.ROLE_NOT_FOUND
                ));

        Page<AppUser> users = appUserRepository.findByRolesContaining(role, pageable);

        if (users.isEmpty()) {
            return Page.empty(pageable);
        }

        return appUserMapper.toDtoPage(users);
    }

    //todo remove ?
    public Page<AppUserDto> getUsersByRoleWithPagination(String roleName, Pageable pageable) {
        if (roleName == null || roleName.isBlank()) {
            throw new ApiException(
                    "Role name cannot be null or blank",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_INPUT
            );
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.NOT_FOUND,
                        Codes.ROLE_NOT_FOUND
                ));

        Page<AppUser> users = appUserRepository.findByRolesContaining(role, pageable);

        if (users.isEmpty()) {
            throw new ApiException(
                    "No users found with role: " + roleName,
                    HttpStatus.NOT_FOUND,
                    Codes.USER_NOT_FOUND
            );
        }

        return appUserMapper.toDtoPage(users);
    }
}