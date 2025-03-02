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
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.login.LoginRequest;
import pl.kamann.dtos.login.LoginResponse;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.services.AuthService;
import pl.kamann.services.ConfirmUserService;
import pl.kamann.services.TokenService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

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
        String email = "user@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRoles(Set.of(clientRole));
        user.setEnabled(true);

        LoginRequest loginRequest = new LoginRequest(email, password);

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(user, encodedPassword, user.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuthentication);

        when(jwtUtils.generateToken(email, user.getRoles())).thenReturn("token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token", response.token());

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtUtils).generateToken(email, user.getRoles());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNotEnabledDuringLogin() {
        String email = "user@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setEnabled(false);

        LoginRequest loginRequest = new LoginRequest(email, password);

        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new ApiException("Email not confirmed.", HttpStatus.UNAUTHORIZED, AuthCodes.EMAIL_NOT_CONFIRMED.getCode()));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));
        assertEquals("Email not confirmed.", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenEmailNotFoundDuringLogin() {
        String email = "nonexistent@example.com";
        LoginRequest loginRequest = new LoginRequest(email, "password");

        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new ApiException("Invalid email address.", HttpStatus.NOT_FOUND, "INVALID_EMAIL"));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));
        assertEquals("Invalid email address.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalidDuringLogin() {
        String email = "user@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        LoginRequest loginRequest = new LoginRequest(email, password);

        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new ApiException("Invalid password.", HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD"));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));

        assertEquals("Invalid password.", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verifyNoInteractions(jwtUtils);
        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("client@example.com", "password", "John", "Doe", "CLIENT");

        AppUser savedUser = AppUser.builder()
                .id(1L)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password("encodedPassword")
                .roles(Set.of(clientRole))
                .status(AppUserStatus.ACTIVE)
                .build();

        AppUserDto expectedDto = AppUserDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .roles(savedUser.getRoles())
                .status(savedUser.getStatus())
                .build();

        when(appUserRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(appUserMapper.toDto(any(AppUser.class))).thenReturn(expectedDto);

        AppUserDto registeredUser = authService.registerUser(request);

        assertNotNull(registeredUser);
        assertEquals(expectedDto.id(), registeredUser.id());
        assertEquals(expectedDto.email(), registeredUser.email());
        assertEquals(expectedDto.firstName(), registeredUser.firstName());
        assertEquals(expectedDto.lastName(), registeredUser.lastName());
        assertEquals(expectedDto.roles(), registeredUser.roles());
        assertEquals(expectedDto.status(), registeredUser.status());

        verify(appUserRepository).findByEmail(request.email());
        verify(roleRepository).findByName("CLIENT");
        verify(passwordEncoder).encode(request.password());
        verify(appUserRepository).save(any(AppUser.class));
        verify(confirmUserService).sendConfirmationEmail(any(AppUser.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyRegisteredDuringRegistration() {
        String email = "existing@example.com";
        RegisterRequest request = new RegisterRequest(email, "password", "John", "Doe", clientRole.getName());

        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(new AppUser()));

        ApiException exception = assertThrows(ApiException.class, () -> authService.registerUser(request));
        assertEquals("Email is already registered: " + email, exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(appUserRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder, roleRepository);
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFoundDuringRegistration() {
        String email = "new@example.com";
        String roleName = "UNKNOWN_ROLE";
        Role unknownRole = new Role();
        unknownRole.setName(roleName);

        RegisterRequest request = new RegisterRequest(email, "password", "John", "Doe", unknownRole.getName());

        when(appUserRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> authService.registerUser(request));
        assertEquals("Role not found: " + roleName, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(appUserRepository).findByEmail(email);
        verify(roleRepository).findByName(roleName);
        verifyNoInteractions(passwordEncoder);
    }
}
