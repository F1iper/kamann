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
import pl.kamann.entities.appuser.*;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.services.AppUserService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;
import pl.kamann.utility.PaginationUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

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
        var users = List.of(
                AppUser.builder()
                        .id(1L)
                        .email("email1@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .roles(Set.of(new Role("CLIENT")))
                        .status(AppUserStatus.ACTIVE)
                        .build(),
                AppUser.builder()
                        .id(2L)
                        .email("email2@example.com")
                        .firstName("Jane")
                        .lastName("Smith")
                        .roles(Set.of(new Role("INSTRUCTOR")))
                        .status(AppUserStatus.INACTIVE)
                        .build()
        );

        int size = users.size();
        Pageable pageable = PageRequest.of(0, size);
        Page<AppUser> pagedUsers = new PageImpl<>(users, pageable, users.size());

        when(paginationService.validatePageable(pageable)).thenReturn(pageable);
        when(appUserRepository.findAllWithRoles(pageable)).thenReturn(pagedUsers);
        when(paginationUtil.toPaginatedResponse(eq(pagedUsers), any(Function.class)))
                .thenReturn(new PaginatedResponseDto<>(List.of(), new PaginationMetaData(1, users.size())));

        var result = appUserService.getUsers(pageable, null);

        assertNotNull(result);
        assertEquals(users.size(), result.getMetaData().getTotalElements());
        assertEquals(1, result.getMetaData().getTotalPages());

        verify(paginationService).validatePageable(pageable);
        verify(appUserRepository).findAllWithRoles(pageable);
        verify(paginationUtil).toPaginatedResponse(eq(pagedUsers), any(Function.class));
    }

    @Test
    void getUserByIdReturnsUserDto() {
        Long userId = 1L;
        var user = new AppUser();
        when(entityLookupService.findUserById(userId)).thenReturn(user);

        var userDto = new AppUserDto(1L, "email@example.com", "Test", "User", Set.of(new Role("CLIENT")), AppUserStatus.ACTIVE);
        when(appUserMapper.toAppUserDto(user)).thenReturn(userDto);

        var result = appUserService.getUserById(userId);

        assertEquals(userDto, result);
        verify(entityLookupService, times(1)).findUserById(userId);
        verify(appUserMapper, times(1)).toAppUserDto(user);
    }

    @Test
    void createUserSavesUserAndReturnsDto() {
        var request = new RegisterRequest("test@example.com", "password123", "Test", "User", "CLIENT");
        var role = new Role("CLIENT");

        doNothing().when(entityLookupService).validateEmailNotTaken(request.email());
        when(roleRepository.findByName(request.role().toUpperCase())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        var user = new AppUser();
        user.setEmail(request.email());
        user.setPassword("encodedPassword");
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(Set.of(role));
        user.setStatus(AppUserStatus.ACTIVE);
        user.setEnabled(false);

        when(appUserRepository.save(any(AppUser.class))).thenReturn(user);

        var expectedUserDto = new AppUserDto(1L, request.email(), request.firstName(), request.lastName(), Set.of(role), AppUserStatus.ACTIVE);
        when(appUserMapper.toAppUserDto(user)).thenReturn(expectedUserDto);

        var result = appUserService.createUser(request);

        assertEquals(expectedUserDto, result);
        verify(entityLookupService).validateEmailNotTaken(request.email());
        verify(roleRepository).findByName(request.role().toUpperCase());
        verify(passwordEncoder).encode(request.password());
        verify(appUserRepository).save(any(AppUser.class));
        verify(appUserMapper).toAppUserDto(user);
    }

    @Test
    void activateUserChangesStatusToActive() {
        Long userId = 1L;
        var user = new AppUser();
        user.setStatus(AppUserStatus.INACTIVE);
        when(entityLookupService.findUserById(userId)).thenReturn(user);

        appUserService.activateUser(userId);

        assertEquals(AppUserStatus.ACTIVE, user.getStatus());
        verify(appUserRepository, times(1)).save(user);
    }

    @Test
    void deactivateUserChangesStatusToInactive() {
        Long userId = 1L;
        var user = new AppUser();
        user.setStatus(AppUserStatus.ACTIVE);
        when(entityLookupService.findUserById(userId)).thenReturn(user);

        appUserService.deactivateUser(userId);

        assertEquals(AppUserStatus.INACTIVE, user.getStatus());
        verify(appUserRepository, times(1)).save(user);
    }

    @Test
    void createUserThrowsExceptionWhenEmailIsNull() {
        var request = new RegisterRequest(null, "password", "Test", "User", "CLIENT");

        var exception = assertThrows(ApiException.class, () -> appUserService.createUser(request));

        assertEquals("Email cannot be null or blank", exception.getMessage());
        verifyNoInteractions(appUserRepository);
    }

    @Test
    void getUsersByRoleReturnsEmptyResponseWhenNoUsersExist() {
        var pageable = Pageable.unpaged();
        var role = new Role("INSTRUCTOR");

        when(roleRepository.findByName("INSTRUCTOR")).thenReturn(Optional.of(role));
        when(appUserRepository.findByRolesContaining(role, pageable)).thenReturn(Page.empty(pageable));
        when(appUserMapper.toPaginatedResponseDto(any())).thenReturn(new PaginatedResponseDto<>(List.of(), new PaginationMetaData(0, 0)));

        var result = appUserService.getUsersByRole("INSTRUCTOR", pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getMetaData().getTotalPages());
        assertEquals(0, result.getMetaData().getTotalElements());

        verify(roleRepository, times(1)).findByName("INSTRUCTOR");
        verify(appUserRepository, times(1)).findByRolesContaining(role, pageable);
        verify(appUserMapper, times(1)).toPaginatedResponseDto(any());
        verifyNoMoreInteractions(appUserRepository, roleRepository, appUserMapper);
    }
}
