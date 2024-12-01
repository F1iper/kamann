package pl.kamann.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pl.kamann.entities.Role;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.entities.AppUser;
import pl.kamann.entities.AppUserStatus;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.AppUserService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    private AppUserRepository appUserRepository;
    private RoleRepository roleRepository;
    private AppUserMapper appUserMapper;
    private EntityLookupService entityLookupService;
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        roleRepository = mock(RoleRepository.class);
        appUserMapper = mock(AppUserMapper.class);
        entityLookupService = mock(EntityLookupService.class);
        appUserService = new AppUserService(appUserRepository, roleRepository, appUserMapper, entityLookupService);
    }

    @Test
    void shouldGetAllUsers() {
        List<AppUser> users = List.of(new AppUser(), new AppUser());
        when(appUserRepository.findAll()).thenReturn(users);
        when(appUserMapper.toDtoList(users)).thenReturn(List.of(new AppUserDto(), new AppUserDto()));

        List<AppUserDto> result = appUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(appUserRepository).findAll();
        verify(appUserMapper).toDtoList(users);
    }

    @Test
    void shouldGetUserById() {
        AppUser user = new AppUser();
        user.setId(1L);
        when(entityLookupService.findUserById(1L)).thenReturn(user);
        when(appUserMapper.toDto(user)).thenReturn(new AppUserDto());

        AppUserDto result = appUserService.getUserById(1L);

        assertNotNull(result);
        verify(entityLookupService).findUserById(1L);
        verify(appUserMapper).toDto(user);
    }

    @Test
    void shouldCreateUser() {
        AppUserDto userDto = new AppUserDto();
        Role mockRole = new Role();
        mockRole.setName("ROLE_USER");
        userDto.setRoles(Set.of(mockRole));

        AppUser user = new AppUser();
        AppUser savedUser = new AppUser();
        savedUser.setId(1L);

        when(entityLookupService.findRolesByNameIn(userDto.getRoles())).thenReturn(Set.of(mockRole));
        when(appUserMapper.toEntity(userDto, Set.of(mockRole))).thenReturn(user);
        when(appUserRepository.save(user)).thenReturn(savedUser);
        when(appUserMapper.toDto(savedUser)).thenReturn(userDto);

        AppUserDto result = appUserService.createUser(userDto);

        assertNotNull(result);
        verify(entityLookupService).findRolesByNameIn(userDto.getRoles());
        verify(appUserRepository).save(user);
        verify(appUserMapper).toDto(savedUser);
    }

//    @Test
//    void validateEmailNotTakenShouldThrowExceptionWhenEmailExists() {
//        String email = "test@example.com";
//        when(appUserRepository.findByEmail(email))
//                .thenReturn(Optional.of(new AppUser()));
//
//        ApiException exception = assertThrows(ApiException.class,
//                () -> entityLookupService.validateEmailNotTaken(email));
//
//        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
//        assertEquals("Email is already registered: " + email, exception.getMessage());
//        verify(appUserRepository, times(1)).findByEmail(email); // Ensure method is called
//    }

//    @Test
//    void shouldThrowExceptionWhenCreatingUserWithDuplicateEmail() {
//        AppUserDto userDto = new AppUserDto();
//        userDto.setEmail("duplicate@example.com");
//
//        when(appUserRepository.findByEmail("duplicate@example.com")).thenReturn(Optional.of(new AppUser()));
//
//        ApiException exception = assertThrows(ApiException.class, () -> appUserService.createUser(userDto));
//
//        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
//        assertEquals("Email is already registered: duplicate@example.com", exception.getMessage());
//        verify(appUserRepository).findByEmail("duplicate@example.com");
//        verifyNoInteractions(roleRepository, appUserMapper);
//    }

    @Test
    void shouldUpdateUser() {
        AppUserDto userDto = new AppUserDto();
        Role mockRole = new Role();
        mockRole.setName("ROLE_USER");
        userDto.setRoles(Set.of(mockRole));

        AppUser user = new AppUser();
        AppUser updatedUser = new AppUser();

        when(entityLookupService.findUserById(1L)).thenReturn(user);
        when(entityLookupService.findRolesByNameIn(userDto.getRoles())).thenReturn(Set.of(mockRole));
        when(appUserRepository.save(user)).thenReturn(updatedUser);
        when(appUserMapper.toDto(updatedUser)).thenReturn(userDto);

        AppUserDto result = appUserService.updateUser(1L, userDto);

        assertNotNull(result);
        verify(entityLookupService).findUserById(1L);
        verify(entityLookupService).findRolesByNameIn(userDto.getRoles());
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingUserWithInvalidRoles() {
        AppUserDto userDto = new AppUserDto();
        Role invalidRole = new Role();
        invalidRole.setName("INVALID_ROLE");
        userDto.setRoles(Set.of(invalidRole));

        AppUser user = new AppUser();

        when(entityLookupService.findUserById(1L)).thenReturn(user);
        when(entityLookupService.findRolesByNameIn(userDto.getRoles())).thenReturn(Set.of());

        ApiException exception = assertThrows(ApiException.class, () -> appUserService.updateUser(1L, userDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("No valid roles provided for the user.", exception.getMessage());
        verify(entityLookupService).findUserById(1L);
        verify(entityLookupService).findRolesByNameIn(userDto.getRoles());
        verifyNoInteractions(appUserRepository);
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

        AppUserDto updatedUserDto = new AppUserDto();
        updatedUserDto.setId(userId);

        when(entityLookupService.findUserById(userId)).thenReturn(user);
        when(appUserRepository.save(user)).thenReturn(updatedUser);
        when(appUserMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        AppUserDto result = appUserService.changeUserStatus(userId, newStatus);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(entityLookupService).findUserById(userId);
        verify(appUserRepository).save(user);
        verify(appUserMapper).toDto(updatedUser);
    }
}
