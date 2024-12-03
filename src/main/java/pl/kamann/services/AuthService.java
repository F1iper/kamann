package pl.kamann.services;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.dtos.LoginRequest;
import pl.kamann.dtos.LoginResponse;
import pl.kamann.dtos.RegisterRequest;
import pl.kamann.entities.appuser.Role;
import pl.kamann.repositories.RoleRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.repositories.AppUserRepository;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;

    public LoginResponse login(@Valid LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(
                        "Invalid email address.",
                        HttpStatus.NOT_FOUND,
                        Codes.USER_NOT_FOUND
                ));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Invalid login attempt for email: {}", request.email());
            throw new ApiException(
                    "Invalid password.",
                    HttpStatus.UNAUTHORIZED,
                    Codes.UNAUTHORIZED
            );
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRoles());
        log.info("User logged in successfully: email={}", user.getEmail());
        return new LoginResponse(token);
    }

    @Transactional
    public AppUser registerUser(RegisterRequest request) {
        validateEmailNotTaken(request.email());

        String roleName = request.role() != null ? request.role() : Codes.CLIENT;
        Role userRole = findRoleByName(roleName);

        AppUser user = createAppUser(request, userRole);
        AppUser savedUser = appUserRepository.save(user);

        log.info("User registered successfully: email={}, role={}", request.email(), userRole.getName());
        return savedUser;
    }

    private AppUser createAppUser(RegisterRequest request, Role role) {
        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(Set.of(role));
        user.setStatus(AppUserStatus.ACTIVE);
        return user;
    }

    private void validateEmailNotTaken(String email) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            log.warn("Attempted registration with existing email: {}", email);
            throw new ApiException(
                    "Email is already registered: " + email,
                    HttpStatus.CONFLICT,
                    Codes.EMAIL_ALREADY_EXISTS
            );
        }
    }

    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.NOT_FOUND,
                        Codes.ROLE_NOT_FOUND
                ));
    }
}
