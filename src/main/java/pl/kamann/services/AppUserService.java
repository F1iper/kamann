package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import pl.kamann.utility.PaginationService;
import pl.kamann.utility.PaginationUtil;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final RoleRepository roleRepository;

    private final EntityLookupService entityLookupService;

    private final PaginationService paginationService;
    private final PaginationUtil paginationUtil;

    public PaginatedResponseDto<AppUserDto> getUsers(Pageable pageable, String roleName) {
        pageable = paginationService.validatePageable(pageable);

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

        return paginationUtil.toPaginatedResponse(pagedUsers, this::mapToDto);
    }

    private AppUserDto mapToDto(AppUser appUser) {
        return new AppUserDto(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getRoles(),
                appUser.getStatus()
        );
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
        return appUserMapper.mapToDto(user);
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

        AppUser user = appUserMapper.mapToEntity(userDto, roles);

        if (user.getStatus() == null) {
            user.setStatus(AppUserStatus.ACTIVE);
        }

        return appUserMapper.mapToDto(appUserRepository.save(user));
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
        return appUserMapper.mapToDto(appUserRepository.save(user));
    }

    public void activateUser(Long userId) {
        changeUserStatus(userId, AppUserStatus.ACTIVE);
    }

    public void deactivateUser(Long userId) {
        changeUserStatus(userId, AppUserStatus.INACTIVE);
    }

    public PaginatedResponseDto<AppUserDto> getUsersByRole(String roleName, Pageable pageable) {
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

        PaginationMetaData metaData = new PaginationMetaData(users.getTotalPages(), users.getTotalElements());

        return appUserMapper.mapToDtoPaginatedResponseDto(new PaginatedResponseDto<>(users.getContent(), metaData));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}