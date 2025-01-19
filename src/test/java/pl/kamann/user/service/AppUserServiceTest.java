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
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.services.AppUserService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void getAllUsersReturnsPaginatedResponseDto() {
        // Given
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

        int page = 1;
        int size = users.size();
        PageRequest pageRequest = PageRequest.of(page - 1, size);

        when(appUserRepository.findAllWithRoles(pageRequest))
                .thenReturn(new PageImpl<>(users, pageRequest, users.size()));

        // When
        var result = appUserService.getUsers(page, size, null);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(users.size(), result.getMetaData().getTotalElements(),
                "Total elements should match the size of the users list");
        assertEquals(1, result.getMetaData().getTotalPages(), "Total pages should be 1");

        verify(appUserRepository, times(1)).findAllWithRoles(pageRequest);
    }


    @Test
    void getUserByIdReturnsUserDto() {
        Long userId = 1L;
        var user = new AppUser();
        when(entityLookupService.findUserById(userId)).thenReturn(user);

        var userDto = new AppUserDto(1L, "email@example.com", "Test", "User", Set.of(new Role("CLIENT")), AppUserStatus.ACTIVE);
        when(appUserMapper.toDto(user)).thenReturn(userDto);

        var result = appUserService.getUserById(userId);

        assertEquals(userDto, result);
        verify(entityLookupService, times(1)).findUserById(userId);
        verify(appUserMapper, times(1)).toDto(user);
    }

    @Test
    void createUserSavesUserAndReturnsDto() {
        var userDto = new AppUserDto(null, "test@example.com", "Test", "User", Set.of(new Role("CLIENT")), AppUserStatus.ACTIVE);
        var roles = Set.of(new Role("CLIENT"));

        var user = new AppUser();
        when(appUserMapper.toEntity(any(AppUserDto.class), eq(roles))).thenReturn(user);

        var savedUser = new AppUser();
        savedUser.setId(1L);
        when(appUserRepository.save(user)).thenReturn(savedUser);

        var savedUserDto = new AppUserDto(1L, "test@example.com", "Test", "User", roles, AppUserStatus.ACTIVE);
        when(appUserMapper.toDto(savedUser)).thenReturn(savedUserDto);

        when(entityLookupService.findRolesByNameIn(userDto.roles())).thenReturn(roles);

        var result = appUserService.createUser(userDto);

        assertEquals(savedUserDto, result);
        verify(entityLookupService, times(1)).validateEmailNotTaken(userDto.email());
        verify(entityLookupService, times(1)).findRolesByNameIn(userDto.roles());
        verify(appUserMapper, times(1)).toEntity(eq(userDto), eq(roles));
        verify(appUserRepository, times(1)).save(user);
        verify(appUserMapper, times(1)).toDto(savedUser);
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
        var userDto = new AppUserDto(null, null, "Test", "User", Set.of(new Role("CLIENT")), AppUserStatus.ACTIVE);

        var exception = assertThrows(ApiException.class, () -> appUserService.createUser(userDto));

        assertEquals("Email cannot be null or blank", exception.getMessage());
        verifyNoInteractions(appUserRepository);
    }

    @Test
    void getUsersByRoleReturnsEmptyPageWhenNoUsersExist() {
        var pageable = Pageable.unpaged();
        var role = new Role("INSTRUCTOR");

        when(roleRepository.findByName("INSTRUCTOR")).thenReturn(Optional.of(role));
        when(appUserRepository.findByRolesContaining(role, pageable)).thenReturn(Page.empty(pageable));

        var result = appUserService.getUsersByRole("INSTRUCTOR", pageable);

        assertTrue(result.isEmpty());
        verify(roleRepository, times(1)).findByName("INSTRUCTOR");
        verify(appUserRepository, times(1)).findByRolesContaining(role, pageable);
        verifyNoMoreInteractions(appUserRepository, roleRepository);
    }
}
