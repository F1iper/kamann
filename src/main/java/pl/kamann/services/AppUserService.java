package pl.kamann.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.StatusCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.config.pagination.PaginationMetaData;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.AuthUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AuthUserRepository;
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

    private final PasswordEncoder passwordEncoder;
    private final EntityLookupService entityLookupService;

    private final PaginationService paginationService;
    private final PaginationUtil paginationUtil;
    private final AuthUserRepository authUserRepository;

    public PaginatedResponseDto<AppUserDto> getUsers(Pageable pageable, String roleName) {

        pageable = paginationService.validatePageable(pageable);

        Page<AuthUser> pagedAuthUsers;

        if (roleName == null || roleName.isEmpty()) {
            pagedAuthUsers = authUserRepository.findAll(pageable);
        } else {
            Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new ApiException(
                            "Role not found: " + roleName,
                            HttpStatus.NOT_FOUND,
                            StatusCodes.NO_RESULTS.name()));

            pagedAuthUsers = authUserRepository.findUsersByRoleWithRoles(pageable, role);
        }

        return paginationUtil.toPaginatedResponse(pagedAuthUsers, this::mapAuthUserToAppUserDto);
    }

    private AppUserDto mapAuthUserToAppUserDto(AuthUser authUser) {
        AppUser appUser = authUser.getAppUser();

        if (appUser == null) {
            throw new ApiException("AppUser not found for AuthUser with ID: " + authUser.getId(),
                    HttpStatus.NOT_FOUND,
                    AuthCodes.USER_NOT_FOUND.name());
        }

        return appUserMapper.toAppUserDto(appUser);
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
        return appUserMapper.toAppUserDto(user);
    }

    public AppUserDto createUser(RegisterRequest request, String providedRole) {
        if (request.email() == null || request.email().isBlank()) {
            throw new ApiException(
                    "Email cannot be null or blank",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        if (providedRole == null || providedRole.isBlank()) {
            throw new ApiException(
                    "Role cannot be null or blank",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name()
            );
        }

        entityLookupService.validateEmailNotTaken(request.email());

        Role role = roleRepository.findByName(providedRole.toUpperCase())
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + providedRole,
                        HttpStatus.NOT_FOUND,
                        AuthCodes.ROLE_NOT_FOUND.name()
                ));

        String encodedPassword = passwordEncoder.encode(request.password());
        AuthUser authUser = new AuthUser();
        authUser.setEmail(request.email());
        authUser.setPassword(encodedPassword);
        authUser.setStatus(AuthUserStatus.PENDING);
        authUser.setEnabled(false);
        authUser.setRoles(Set.of(role));

        AppUser appUser = new AppUser();
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());
        appUser.setAuthUser(authUser);

        AppUser savedUser = appUserRepository.save(appUser);

        return appUserMapper.toAppUserDto(savedUser);
    }

    @Transactional
    public AppUserDto changeUserStatus(Long userId, AuthUserStatus status) {
        if (userId == null) {
            throw new ApiException("User ID cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name());
        }
        if (status == null) {
            throw new ApiException("Status cannot be null",
                    HttpStatus.BAD_REQUEST,
                    StatusCodes.INVALID_INPUT.name());
        }

        AppUser user = entityLookupService.findUserById(userId);
        AuthUser authUser = user.getAuthUser();
        if (authUser == null) {
            throw new ApiException("Authentication data not found",
                    HttpStatus.NO_CONTENT,
                    AuthCodes.USER_NOT_FOUND.name());
        }

        authUser.setStatus(status);
        appUserRepository.save(user);

        return appUserMapper.toAppUserDto(user);
    }

    public void activateUser(Long userId) {
        changeUserStatus(userId, AuthUserStatus.ACTIVE);
    }

    public void deactivateUser(Long userId) {
        changeUserStatus(userId, AuthUserStatus.INACTIVE);
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

        Page<AuthUser> authUsers = authUserRepository.findByRolesContaining(role, pageable);

        Page<AppUser> users = authUsers.map(AuthUser::getAppUser);

        PaginationMetaData metaData = new PaginationMetaData(users.getTotalPages(), users.getTotalElements());

        return appUserMapper.toPaginatedResponseDto(new PaginatedResponseDto<>(users.getContent(), metaData));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}