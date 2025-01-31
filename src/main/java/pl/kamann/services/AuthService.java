package pl.kamann.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.RoleCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.AppUserResponseDto;
import pl.kamann.dtos.login.LoginRequest;
import pl.kamann.dtos.login.LoginResponse;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Role;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.RoleRepository;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private final AppUserMapper appUserMapper;

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;

    public LoginResponse login(@Valid LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(
                        "Invalid email address.",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()
                ));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Invalid login attempt for email: {}", request.email());
            throw new ApiException(
                    "Invalid password.",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.UNAUTHORIZED.name()
            );
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRoles());
        log.info("User logged in successfully: email={}", user.getEmail());
        return new LoginResponse(token);
    }

    @Transactional
    public AppUser registerUser(RegisterRequest request) {
        validateEmailNotTaken(request.email());

        String roleName = request.role() != null ? request.role() : RoleCodes.CLIENT.name();
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
                    AuthCodes.EMAIL_ALREADY_EXISTS.name()
            );
        }
    }

    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(
                        "Role not found: " + roleName,
                        HttpStatus.NOT_FOUND,
                        AuthCodes.ROLE_NOT_FOUND.name()
                ));
    }

    public AppUserResponseDto getLoggedInAppUser(HttpServletRequest request) {
        String token = jwtUtils.extractTokenFromRequest(request);

        if (token == null || !jwtUtils.validateToken(token)) {
            throw new ApiException("Invalid or missing token",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.INVALID_TOKEN.name());
        }

        String email = jwtUtils.extractEmail(token);

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()));

        return appUserMapper.toResponseDto(appUser);
    }
}
