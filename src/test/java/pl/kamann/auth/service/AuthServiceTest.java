package pl.kamann.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.RoleCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.login.LoginRequest;
import pl.kamann.dtos.login.LoginResponse;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.AuthUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AuthUserRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.services.AuthService;
import pl.kamann.services.ConfirmUserService;
import pl.kamann.services.TokenService;
import pl.kamann.utility.EntityLookupService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EntityLookupService entityLookupService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ConfirmUserService confirmUserService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AppUserMapper appUserMapper;

    @InjectMocks
    private AuthService authService;

    private Role clientRole;

    @BeforeEach
    void setUp() {
        clientRole = new Role();
        clientRole.setName("CLIENT");
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
        AuthUser user = AuthUser.builder()
                .email(loginRequest.email())
                .password("encodedPassword")
                .roles(Set.of(clientRole))
                .enabled(true)
                .build();

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(user, "encodedPassword", user.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(loginRequest.email(), user.getRoles())).thenReturn("token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token", response.token());

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtUtils).generateToken(loginRequest.email(), user.getRoles());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNotEnabledDuringLogin() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new ApiException("Email not confirmed.", HttpStatus.UNAUTHORIZED, AuthCodes.EMAIL_NOT_CONFIRMED.getCode()));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));

        assertEquals("Email not confirmed.", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailNotFoundDuringLogin() {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new ApiException("Invalid email address.", HttpStatus.NOT_FOUND, "INVALID_EMAIL"));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));

        assertEquals("Invalid email address.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalidDuringLogin() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new ApiException("Invalid password.", HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD"));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));

        assertEquals("Invalid password.", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void shouldRegisterClientSuccessfully() {
        RegisterRequest request = new RegisterRequest("client@example.com", "password", "John", "Doe", "123-456-7890");

        AppUser savedUser = AppUser.builder()
                .id(1L)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();

        AuthUser savedAuthUser = AuthUser.builder()
                .id(2L)
                .email(request.email())
                .password("encodedPassword")
                .roles(Set.of(clientRole))
                .enabled(false)
                .status(AuthUserStatus.ACTIVE)
                .appUser(savedUser)
                .build();

        savedUser.setAuthUser(savedAuthUser);

        AppUserDto expectedDto = AppUserDto.builder()
                .id(savedUser.getId())
                .email(savedAuthUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phone(savedUser.getPhone())
                .status(savedAuthUser.getStatus())
                .build();

        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        doNothing().when(entityLookupService).validateEmailNotTaken(request.email());
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            user.getAuthUser().setId(2L);
            return user;
        });
        when(appUserMapper.toAppUserDto(any(AppUser.class))).thenReturn(expectedDto);

        AppUserDto registeredUser = authService.registerClient(request);

        assertNotNull(registeredUser);
        assertEquals(expectedDto.id(), registeredUser.id());
        assertEquals(expectedDto.email(), registeredUser.email());
        assertEquals(expectedDto.firstName(), registeredUser.firstName());
        assertEquals(expectedDto.lastName(), registeredUser.lastName());
        assertEquals(expectedDto.phone(), registeredUser.phone());
        assertEquals(expectedDto.status(), registeredUser.status());

        verify(entityLookupService).validateEmailNotTaken(request.email());
        verify(roleRepository).findByName("CLIENT");
        verify(passwordEncoder).encode(request.password());
        verify(appUserRepository).save(any(AppUser.class));
        verify(appUserMapper).toAppUserDto(any(AppUser.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyRegisteredDuringRegistration() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password", "John", "Doe", "123-456-7890");

        doThrow(new ApiException("Email is already registered: " + request.email(), HttpStatus.CONFLICT, AuthCodes.EMAIL_ALREADY_EXISTS.getCode()))
                .when(entityLookupService).validateEmailNotTaken(request.email());

        ApiException exception = assertThrows(ApiException.class, () -> authService.registerClient(request));

        assertEquals("Email is already registered: " + request.email(), exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(entityLookupService).validateEmailNotTaken(request.email());
        verifyNoInteractions(passwordEncoder, roleRepository, appUserRepository);
    }

    @Test
    void shouldThrowExceptionWhenClientRoleNotFoundDuringRegistration() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password", "John", "Doe", "123-456-7890");

        doNothing().when(entityLookupService).validateEmailNotTaken(request.email());
        when(roleRepository.findByName(RoleCodes.CLIENT.name())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> authService.registerClient(request));

        assertEquals("Role not found: " + RoleCodes.CLIENT.name(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(entityLookupService).validateEmailNotTaken(request.email());
        verify(roleRepository).findByName(RoleCodes.CLIENT.name());
        verifyNoInteractions(passwordEncoder, appUserRepository, confirmUserService);
    }

    @Test
    void shouldThrowExceptionWhenInstructorRoleNotFoundDuringRegistration() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password", "John", "Doe", "123-456-7890");

        doNothing().when(entityLookupService).validateEmailNotTaken(request.email());
        when(roleRepository.findByName(RoleCodes.INSTRUCTOR.name())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> authService.registerInstructor(request));

        assertEquals("Role not found: " + RoleCodes.INSTRUCTOR.name(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(entityLookupService).validateEmailNotTaken(request.email());
        verify(roleRepository).findByName(RoleCodes.INSTRUCTOR.name());
        verifyNoInteractions(passwordEncoder, appUserRepository);
    }
}