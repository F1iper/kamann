package pl.kamann.services;

import jakarta.persistence.EntityNotFoundException;
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
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final EntityLookupService entityLookupService;
    private final PaginationService paginationService;

    public Page<AppUserDto> getAllUsers(Pageable pageable) {
        return appUserRepository.findAll(pageable)
                .map(appUserMapper::toDto);
    }

    public AppUserDto getUserById(Long id) {
        AppUser user = entityLookupService.findUserById(id);
        return appUserMapper.toDto(user);
    }

    public AppUserDto createUser(AppUserDto userDto) {
        entityLookupService.validateEmailNotTaken(userDto.email());

        Set<Role> roles = entityLookupService.findRolesByNameIn(userDto.roles());

        AppUser user = appUserMapper.toEntity(userDto, roles);

        if (user.getStatus() == null) {
            user.setStatus(AppUserStatus.ACTIVE);
        }

        return appUserMapper.toDto(appUserRepository.save(user));
    }

    public AppUserDto updateUser(Long id, AppUserDto userDto) {
        AppUser existingUser = entityLookupService.findUserById(id);

        Set<Role> roles = entityLookupService.findRolesByNameIn(userDto.roles());
        if (roles.isEmpty()) {
            throw new ApiException(
                    "No valid roles provided for the user.",
                    HttpStatus.BAD_REQUEST,
                    Codes.INVALID_ROLE);
        }

        existingUser.setEmail(userDto.email());
        existingUser.setFirstName(userDto.firstName());
        existingUser.setLastName(userDto.lastName());
        existingUser.setRoles(roles);

        return appUserMapper.toDto(appUserRepository.save(existingUser));
    }

    public void activateUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found with ID: " + userId,
                        HttpStatus.NOT_FOUND,
                        Codes.USER_NOT_FOUND));
        user.setStatus(AppUserStatus.ACTIVE);
        appUserRepository.save(user);
    }

    public void deactivateUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found with ID: " + userId,
                        HttpStatus.NOT_FOUND,
                        Codes.USER_NOT_FOUND));
        user.setStatus(AppUserStatus.INACTIVE);
        appUserRepository.save(user);
    }

    public Page<AppUserDto> getUsersByRole(String roleName, Pageable pageable) {
        Pageable validatedPageable = paginationService.validatePageable(pageable);
        Role role = entityLookupService.findRoleByName(roleName);
        Page<AppUser> users = appUserRepository.findByRolesContaining(role, validatedPageable);
        return users.map(appUserMapper::toDto);
    }


    public AppUserDto changeUserStatus(Long userId, AppUserStatus status) {
        AppUser user = entityLookupService.findUserById(userId);
        user.setStatus(status);
        return appUserMapper.toDto(appUserRepository.save(user));
    }
}