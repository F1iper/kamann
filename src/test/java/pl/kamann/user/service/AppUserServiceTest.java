package pl.kamann.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import pl.kamann.services.AppUserService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;
import pl.kamann.utility.PaginationUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AppUserMapper appUserMapper;

    @Mock
    private EntityLookupService entityLookupService;

    @Mock
    private PaginationService paginationService;

    @Mock
    private PaginationUtil paginationUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void getAllUsersReturnsPaginatedResponseDto() {
        Role clientRole = new Role("CLIENT");
        Role instructorRole = new Role("INSTRUCTOR");

        AuthUser authUser1 = AuthUser.builder()
                .email("email1@example.com")
                .roles(Set.of(clientRole))
                .status(AuthUserStatus.ACTIVE)
                .build();
        AppUser user1 = AppUser.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .authUser(authUser1)
                .build();
        authUser1.setAppUser(user1);

        AuthUser authUser2 = AuthUser.builder()
                .email("email2@example.com")
                .roles(Set.of(instructorRole))
                .status(AuthUserStatus.INACTIVE)
                .build();
        AppUser user2 = AppUser.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .authUser(authUser2)
                .build();
        authUser2.setAppUser(user2);

        var authUsers = List.of(authUser1, authUser2);
        int size = authUsers.size();
        Pageable pageable = PageRequest.of(0, size);
        Page<AuthUser> pagedAuthUsers = new PageImpl<>(authUsers, pageable, size);

        when(paginationService.validatePageable(pageable)).thenReturn(pageable);

        when(authUserRepository.findAll(pageable)).thenReturn(pagedAuthUsers);

        List<AppUserDto> appUserDtos = authUsers.stream()
                .map(AuthUser::getAppUser)
                .map(appUser -> new AppUserDto(
                        appUser.getId(),
                        appUser.getFirstName(),
                        appUser.getLastName(),
                        appUser.getAuthUser().getEmail(),
                        appUser.getAuthUser().getStatus(),
                        appUser.getAuthUser().getRoles().stream().findFirst().map(Role::getName).orElse(null)
                ))
                .toList();

        PaginatedResponseDto<AppUserDto> expectedResponse = new PaginatedResponseDto<>(
                appUserDtos,
                new PaginationMetaData(1, size)
        );

        doReturn(expectedResponse).when(paginationUtil).toPaginatedResponse(
                eq(pagedAuthUsers),
                any(Function.class)
        );

        var result = appUserService.getUsers(pageable, null);

        // Assertions
        assertNotNull(result);
        assertEquals(size, result.getMetaData().getTotalElements());
        assertEquals(1, result.getMetaData().getTotalPages());

        verify(paginationService).validatePageable(pageable);
        verify(authUserRepository).findAll(pageable);
        verify(paginationUtil).toPaginatedResponse(eq(pagedAuthUsers), any(Function.class));
    }

    @Test
    void getUserByIdReturnsUserDto() {
        Long userId = 1L;
        AuthUser authUser = AuthUser.builder()
                .email("email@example.com")
                .roles(Set.of(new Role("CLIENT")))
                .status(AuthUserStatus.ACTIVE)
                .build();
        AppUser user = AppUser.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .authUser(authUser)
                .build();
        authUser.setAppUser(user);

        when(entityLookupService.findUserById(userId)).thenReturn(user);
        var userDto = new AppUserDto(userId, authUser.getEmail(), user.getFirstName(), user.getLastName(), authUser.getStatus(), user.getPhone());
        when(appUserMapper.toAppUserDto(user)).thenReturn(userDto);

        var result = appUserService.getUserById(userId);

        assertEquals(userDto, result);
        verify(entityLookupService).findUserById(userId);
        verify(appUserMapper).toAppUserDto(user);
    }

    @Test
    void createUserSavesUserAndReturnsDto() {
        var request = new RegisterRequest("test@example.com", "password123", "Test", "User", "123-456-7890");
        var role = new Role("CLIENT");

        doNothing().when(entityLookupService).validateEmailNotTaken(request.email());
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        AuthUser authUser = AuthUser.builder()
                .email(request.email())
                .password("encodedPassword")
                .roles(Set.of(role))
                .status(AuthUserStatus.ACTIVE)
                .enabled(false)
                .build();
        AppUser user = AppUser.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .authUser(authUser)
                .build();
        authUser.setAppUser(user);

        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            savedUser.getAuthUser().setId(2L);
            return savedUser;
        });

        var expectedUserDto = new AppUserDto(1L, request.email(), request.firstName(), request.lastName(), AuthUserStatus.ACTIVE, request.phone());
        when(appUserMapper.toAppUserDto(any(AppUser.class))).thenReturn(expectedUserDto);

        var result = appUserService.createUser(request, "CLIENT");

        assertEquals(expectedUserDto, result);
        verify(entityLookupService).validateEmailNotTaken(request.email());
        verify(roleRepository).findByName("CLIENT");
        verify(passwordEncoder).encode(request.password());
        verify(appUserRepository).save(any(AppUser.class));
        verify(appUserMapper).toAppUserDto(any(AppUser.class));
    }

    @Test
    void activateUserChangesStatusToActive() {
        Long userId = 1L;
        AuthUser authUser = AuthUser.builder()
                .status(AuthUserStatus.INACTIVE)
                .build();
        AppUser user = AppUser.builder()
                .id(userId)
                .authUser(authUser)
                .build();
        authUser.setAppUser(user);

        when(entityLookupService.findUserById(userId)).thenReturn(user);
        when(appUserRepository.save(user)).thenReturn(user);

        appUserService.activateUser(userId);

        assertEquals(AuthUserStatus.ACTIVE, authUser.getStatus());
        verify(entityLookupService).findUserById(userId);
        verify(appUserRepository).save(user);
    }

    @Test
    void deactivateUserChangesStatusToInactive() {
        Long userId = 1L;
        AuthUser authUser = AuthUser.builder()
                .status(AuthUserStatus.ACTIVE)
                .build();
        AppUser user = AppUser.builder()
                .id(userId)
                .authUser(authUser)
                .build();
        authUser.setAppUser(user);

        when(entityLookupService.findUserById(userId)).thenReturn(user);
        when(appUserRepository.save(user)).thenReturn(user);

        appUserService.deactivateUser(userId);

        assertEquals(AuthUserStatus.INACTIVE, authUser.getStatus());
        verify(entityLookupService).findUserById(userId);
        verify(appUserRepository).save(user);
    }

    @Test
    void createUserThrowsExceptionWhenEmailIsNull() {
        var request = new RegisterRequest(null, "password", "Test", "User", "123-456-7890");

        var exception = assertThrows(ApiException.class, () -> appUserService.createUser(request, "INSTRUCTOR"));

        assertEquals("Email cannot be null or blank", exception.getMessage());
        verifyNoInteractions(appUserRepository, roleRepository, passwordEncoder);
    }

    @Test
    void getUsersByRoleReturnsEmptyResponseWhenNoUsersExist() {
        var pageable = Pageable.unpaged();
        var role = new Role("INSTRUCTOR");

        when(roleRepository.findByName("INSTRUCTOR")).thenReturn(Optional.of(role));
        when(authUserRepository.findByRolesContaining(role, pageable)).thenReturn(Page.empty(pageable));
        when(appUserMapper.toPaginatedResponseDto(any())).thenReturn(new PaginatedResponseDto<>(List.of(), new PaginationMetaData(0, 0)));

        var result = appUserService.getUsersByRole("INSTRUCTOR", pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getMetaData().getTotalPages());
        assertEquals(0, result.getMetaData().getTotalElements());

        verify(roleRepository).findByName("INSTRUCTOR");
        verify(authUserRepository).findByRolesContaining(role, pageable);
        verify(appUserMapper).toPaginatedResponseDto(any());
    }

}