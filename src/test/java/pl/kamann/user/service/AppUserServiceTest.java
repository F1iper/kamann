package pl.kamann.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.user.dto.AppUserDto;
import pl.kamann.user.mapper.AppUserMapper;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.model.AppUserStatus;
import pl.kamann.user.repository.AppUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    private AppUserRepository appUserRepository;
    private RoleRepository roleRepository;
    private AppUserMapper appUserMapper;
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        roleRepository = mock(RoleRepository.class);
        appUserMapper = mock(AppUserMapper.class);
        appUserService = new AppUserService(appUserRepository, roleRepository, appUserMapper);
    }

    @Test
    void shouldGetAllUsers() {
        // given
        List<AppUser> users = List.of(new AppUser(), new AppUser());
        when(appUserRepository.findAll()).thenReturn(users);
        when(appUserMapper.toDtoList(users)).thenReturn(List.of(new AppUserDto(), new AppUserDto()));

        // when
        List<AppUserDto> result = appUserService.getAllUsers();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(appUserRepository, times(1)).findAll();
        verify(appUserMapper, times(1)).toDtoList(users);
    }

    @Test
    void shouldGetUserById() {
        // given
        AppUser user = new AppUser();
        user.setId(1L);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(appUserMapper.toDto(user)).thenReturn(new AppUserDto());

        // when
        AppUserDto result = appUserService.getUserById(1L);

        // then
        assertNotNull(result);
        verify(appUserRepository, times(1)).findById(1L);
        verify(appUserMapper, times(1)).toDto(user);
    }

    @Test
    void shouldCreateUser() {
        // given
        AppUserDto userDto = new AppUserDto();
        userDto.setRoles(Set.of(new Role("ROLE_USER")));

        AppUser user = new AppUser();
        AppUser savedUser = new AppUser();
        savedUser.setId(1L);

        Role mockRole = new Role();
        mockRole.setName("ROLE_USER");

        when(roleRepository.findByNameIn(eq(userDto.getRoles())))
                .thenReturn(Set.of(mockRole));

        when(appUserMapper.toEntity(eq(userDto), anySet()))
                .thenReturn(user);

        when(appUserRepository.save(eq(user)))
                .thenReturn(savedUser);

        when(appUserMapper.toDto(eq(savedUser)))
                .thenReturn(userDto);

        // when
        AppUserDto result = appUserService.createUser(userDto);

        // then
        assertNotNull(result);

        // verify
        verify(roleRepository, times(1)).findByNameIn(eq(userDto.getRoles()));
        verify(appUserRepository, times(1)).save(eq(user));
        verify(appUserMapper, times(1)).toDto(eq(savedUser));
    }

    @Test
    void shouldUpdateUser() {
        // given
        AppUserDto userDto = new AppUserDto();
        AppUser user = new AppUser();
        AppUser updatedUser = new AppUser();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByNameIn(any())).thenReturn(Set.of(new Role()));
        when(appUserRepository.save(user)).thenReturn(updatedUser);
        when(appUserMapper.toDto(updatedUser)).thenReturn(userDto);

        // when
        AppUserDto result = appUserService.updateUser(1L, userDto);

        // then
        assertNotNull(result);
        verify(appUserRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByNameIn(any());
        verify(appUserRepository, times(1)).save(user);
    }

    @Test
    void shouldChangeUserStatus() {
        // given
        Long userId = 1L;
        AppUserStatus newStatus = AppUserStatus.INACTIVE;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setStatus(AppUserStatus.ACTIVE);

        AppUser updatedUser = new AppUser();
        updatedUser.setId(userId);
        updatedUser.setStatus(newStatus);

        AppUserDto updatedUserDto = new AppUserDto();
        updatedUserDto.setId(userId);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenReturn(updatedUser);
        when(appUserMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        // when
        AppUserDto result = appUserService.changeUserStatus(userId, newStatus);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(appUserRepository, times(1)).findById(userId);
        verify(appUserRepository, times(1)).save(user);
        verify(appUserMapper, times(1)).toDto(updatedUser);
    }
}