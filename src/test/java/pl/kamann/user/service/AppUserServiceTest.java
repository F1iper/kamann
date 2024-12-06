package pl.kamann.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.AppUserService;
import pl.kamann.utility.EntityLookupService;
import pl.kamann.utility.PaginationService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    private AppUserRepository appUserRepository;
    private PaginationService paginationService;
    private AppUserMapper appUserMapper;
    private EntityLookupService entityLookupService;
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        appUserMapper = mock(AppUserMapper.class);
        entityLookupService = mock(EntityLookupService.class);
        paginationService = mock(PaginationService.class);
        appUserService = new AppUserService(appUserRepository, appUserMapper, entityLookupService, paginationService);
    }

    @Test
    void shouldGetAllUsersPaginated() {
        List<AppUser> users = List.of(new AppUser(), new AppUser());
        Page<AppUser> userPage = new PageImpl<>(users);
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(appUserRepository.findAll(pageRequest)).thenReturn(userPage);
        when(appUserMapper.toDto(any(AppUser.class))).thenAnswer(invocation -> new AppUserDto(null, null, null, null, null, null));

        Page<AppUserDto> result = appUserService.getAllUsers(pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(appUserRepository).findAll(pageRequest);
        verify(appUserMapper, times(2)).toDto(any(AppUser.class));
    }

    @Test
    void shouldGetUserById() {
        AppUser user = new AppUser();
        user.setId(1L);
        when(entityLookupService.findUserById(1L)).thenReturn(user);
        when(appUserMapper.toDto(user)).thenReturn(new AppUserDto(1L, "test@example.com", "John", "Doe", Set.of(), AppUserStatus.ACTIVE));

        AppUserDto result = appUserService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(entityLookupService).findUserById(1L);
        verify(appUserMapper).toDto(user);
    }

    @Test
    void shouldCreateUser() {
        Role mockRole = new Role("ROLE_USER");

        AppUserDto userDto = AppUserDto.builder()
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(mockRole))
                .status(AppUserStatus.ACTIVE)
                .build();

        AppUser user = new AppUser();
        AppUser savedUser = new AppUser();
        savedUser.setId(1L);

        when(entityLookupService.findRolesByNameIn(userDto.roles())).thenReturn(Set.of(mockRole));
        when(appUserMapper.toEntity(userDto, Set.of(mockRole))).thenReturn(user);
        when(appUserRepository.save(user)).thenReturn(savedUser);
        when(appUserMapper.toDto(savedUser)).thenReturn(userDto);

        AppUserDto result = appUserService.createUser(userDto);

        assertNotNull(result);
        assertEquals("user@example.com", result.email());
        verify(entityLookupService).findRolesByNameIn(userDto.roles());
        verify(appUserRepository).save(user);
        verify(appUserMapper).toDto(savedUser);
    }

    @Test
    void shouldUpdateUser() {
        Role mockRole = new Role("ROLE_USER");

        AppUserDto userDto = AppUserDto.builder()
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(mockRole))
                .status(AppUserStatus.ACTIVE)
                .build();

        AppUser user = new AppUser();
        AppUser updatedUser = new AppUser();

        when(entityLookupService.findUserById(1L)).thenReturn(user);
        when(entityLookupService.findRolesByNameIn(userDto.roles())).thenReturn(Set.of(mockRole));
        when(appUserRepository.save(user)).thenReturn(updatedUser);
        when(appUserMapper.toDto(updatedUser)).thenReturn(userDto);

        AppUserDto result = appUserService.updateUser(1L, userDto);

        assertNotNull(result);
        assertEquals("user@example.com", result.email());
        verify(entityLookupService).findUserById(1L);
        verify(entityLookupService).findRolesByNameIn(userDto.roles());
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldChangeUserStatus() {
        Long userId = 1L;
        AppUserStatus newStatus = AppUserStatus.INACTIVE;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setStatus(AppUserStatus.ACTIVE);

        AppUser updatedUser = new AppUser();
        updatedUser.setId(userId);
        updatedUser.setStatus(newStatus);

        AppUserDto updatedUserDto = AppUserDto.builder()
                .id(userId)
                .status(newStatus)
                .build();

        when(entityLookupService.findUserById(userId)).thenReturn(user);
        when(appUserRepository.save(user)).thenReturn(updatedUser);
        when(appUserMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        AppUserDto result = appUserService.changeUserStatus(userId, newStatus);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(AppUserStatus.INACTIVE, result.status());
        verify(entityLookupService).findUserById(userId);
        verify(appUserRepository).save(user);
        verify(appUserMapper).toDto(updatedUser);
    }
}