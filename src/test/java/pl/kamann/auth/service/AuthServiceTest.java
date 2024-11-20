package pl.kamann.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.kamann.auth.login.request.LoginRequest;
import pl.kamann.auth.login.response.LoginResponse;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private List<AppUser> testUsers;
    private List<Role> testRoles;

    @BeforeEach
    void setUp() {
        testRoles = List.of(
                createRole("USER"),
                createRole("INSTRUCTOR"),
                createRole("ADMIN")
        );

        testUsers = List.of(
                createUser("john.doe@example.com", "USER"),
                createUser("jane.smith@example.com", "INSTRUCTOR"),
                createUser("admin@admin.com", "ADMIN")
        );
    }

    private Role createRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    private AppUser createUser(String email, String roleName) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setFirstName(email.split("\\.")[0]);
        user.setLastName("Doe");
        user.setRoles(Set.of(testRoles.stream()
                .filter(role -> role.getName().equals(roleName))
                .findFirst()
                .orElseThrow()));
        return user;
    }

    @ParameterizedTest
    @CsvSource({
            "john.doe@example.com, USER",
            "jane.smith@example.com, INSTRUCTOR",
            "admin@admin.com, ADMIN"
    })
    void loginMultipleUserScenariosSuccess(String email, String roleName) {
        // given
        AppUser user = testUsers.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow();

        LoginRequest loginRequest = new LoginRequest(email, "password");

        when(appUserRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(loginRequest.password(), user.getPassword()))
                .thenReturn(true);

        String expectedToken = "token-" + email;
        when(jwtUtils.generateToken(email, user.getRoles()))
                .thenReturn(expectedToken);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertNotNull(response);
        assertEquals(expectedToken, response.token());

        verify(appUserRepository).findByEmail(email);
        verify(passwordEncoder).matches(loginRequest.password(), user.getPassword());
        verify(jwtUtils).generateToken(email, user.getRoles());
    }

    @Test
    void registerClientMultipleUsersAllowDifferentEmails() {
        // given
        Role userRole = testRoles.stream()
                .filter(role -> role.getName().equals("USER"))
                .findFirst()
                .orElseThrow();

        List<RegisterRequest> registerRequests = List.of(
                new RegisterRequest("unique1@example.com", "password1", "John", "Doe"),
                new RegisterRequest("unique2@example.com", "password2", "Jane", "Smith"),
                new RegisterRequest("unique3@example.com", "password3", "Alice", "Johnson")
        );

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));

        registerRequests.forEach(request ->
                when(appUserRepository.findByEmail(request.email()))
                        .thenReturn(Optional.empty())
        );

        when(passwordEncoder.encode(any()))
                .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        // when
        registerRequests.forEach(authService::registerClient);

        // then
        verify(appUserRepository, times(registerRequests.size())).save(argThat(user ->
                registerRequests.stream()
                        .anyMatch(req ->
                                user.getEmail().equals(req.email()) &&
                                        user.getFirstName().equals(req.firstName()) &&
                                        user.getLastName().equals(req.lastName())
                        )
        ));
    }

    @Test
    void registerInstructorMultipleRoleScenarios() {
        // given
        List<RegisterRequest> instructorRequests = List.of(
                new RegisterRequest("instructor1@example.com", "pass1", "John", "Trainer"),
                new RegisterRequest("instructor2@example.com", "pass2", "Jane", "Coach"),
                new RegisterRequest("instructor3@example.com", "pass3", "Mike", "Mentor")
        );

        Role instructorRole = testRoles.stream()
                .filter(role -> role.getName().equals("INSTRUCTOR"))
                .findFirst()
                .orElseThrow();

        when(roleRepository.findByName("INSTRUCTOR"))
                .thenReturn(Optional.of(instructorRole));

        instructorRequests.forEach(request ->
                when(appUserRepository.findByEmail(request.email()))
                        .thenReturn(Optional.empty())
        );

        when(passwordEncoder.encode(any()))
                .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        // when
        instructorRequests.forEach(authService::registerInstructor);

        // then
        verify(appUserRepository, times(instructorRequests.size())).save(argThat(user ->
                instructorRequests.stream()
                        .anyMatch(req ->
                                user.getEmail().equals(req.email()) &&
                                        user.getRoles().contains(instructorRole)
                        )
        ));
    }

    @Test
    void loginUserWithMultipleRolesTokenGenerationCorrect() {
        // given
        AppUser multiRoleUser = new AppUser();
        multiRoleUser.setEmail("multirole@example.com");
        multiRoleUser.setPassword("encodedPassword");
        multiRoleUser.setRoles(Set.of(
                testRoles.stream().filter(r -> r.getName().equals("USER")).findFirst().orElseThrow(),
                testRoles.stream().filter(r -> r.getName().equals("INSTRUCTOR")).findFirst().orElseThrow()
        ));

        LoginRequest loginRequest = new LoginRequest(multiRoleUser.getEmail(), "password");

        when(appUserRepository.findByEmail(multiRoleUser.getEmail()))
                .thenReturn(Optional.of(multiRoleUser));

        when(passwordEncoder.matches(loginRequest.password(), multiRoleUser.getPassword()))
                .thenReturn(true);

        when(jwtUtils.generateToken(multiRoleUser.getEmail(), multiRoleUser.getRoles()))
                .thenReturn("multi-role-token");

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertNotNull(response);
        assertEquals("multi-role-token", response.token());

        // verify
        verify(jwtUtils).generateToken(
                eq(multiRoleUser.getEmail()),
                argThat(roles ->
                        roles.stream().anyMatch(r -> r.getName().equals("USER")) &&
                                roles.stream().anyMatch(r -> r.getName().equals("INSTRUCTOR"))
                )
        );
    }

    @Test
    void loginEdgeCasesCaseInsensitiveEmail() {
        // given
        AppUser user = testUsers.getFirst();
        String email = user.getEmail();
        String differentCaseEmail = email.toUpperCase();

        lenient().when(appUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        LoginRequest loginRequest = new LoginRequest(differentCaseEmail, "password");

        when(passwordEncoder.matches(loginRequest.password(), user.getPassword()))
                .thenReturn(true);

        when(jwtUtils.generateToken(email, user.getRoles()))
                .thenReturn("case-insensitive-token");

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertNotNull(response);
        assertEquals("case-insensitive-token", response.token());

        // verify
        verify(jwtUtils).generateToken(eq(email), eq(user.getRoles()));
    }
}