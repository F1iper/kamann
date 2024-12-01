package pl.kamann.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.LoginRequest;
import pl.kamann.dtos.LoginResponse;
import pl.kamann.dtos.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.services.AuthService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        LoginRequest loginRequest = new LoginRequest(email, password);

        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtils.generateToken(email, user.getRoles())).thenReturn("token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token", response.token());

        verify(appUserRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtUtils).generateToken(email, user.getRoles());
    }

    @Test
    void shouldThrowExceptionWhenEmailNotFoundDuringLogin() {
        String email = "nonexistent@example.com";
        LoginRequest loginRequest = new LoginRequest(email, "password");

        when(appUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));
        assertEquals("Invalid email address.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(appUserRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder, jwtUtils);
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

        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(loginRequest));
        assertEquals("Invalid password.", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(appUserRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("client@example.com", "password", "John", "Doe", clientRole.getName());

        when(appUserRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName(clientRole.getName())).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser registeredUser = authService.registerUser(request);

        assertNotNull(registeredUser);
        assertEquals(request.email(), registeredUser.getEmail());
        assertEquals(clientRole.getName(), registeredUser.getRoles().iterator().next().getName());
        assertEquals(AppUserStatus.ACTIVE, registeredUser.getStatus());

        verify(appUserRepository).findByEmail(request.email());
        verify(roleRepository).findByName(clientRole.getName());
        verify(passwordEncoder).encode(request.password());
        verify(appUserRepository).save(registeredUser);
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
