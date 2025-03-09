package pl.kamann.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.codes.RoleCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.AppUserResponseDto;
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
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    private final AppUserMapper appUserMapper;
    private final ConfirmUserService confirmUserService;

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final AuthUserRepository authUserRepository;

    private final EntityLookupService lookupService;

    public LoginResponse login(@Valid LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            log.info("User logged in successfully: email={}", authUser.getEmail());
            return new LoginResponse(jwtUtils.generateToken(authUser.getEmail(), authUser.getRoles()));
        } catch (DisabledException e) {
            log.warn("Attempted login with unconfirmed email: {}", request.email());
            throw new ApiException(
                    "Email not confirmed.",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.EMAIL_NOT_CONFIRMED.name()
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid User credentials attempt for email: {}", request.email());
            throw new ApiException(
                    "Invalid user credentials.",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.UNAUTHORIZED.name()
            );
        }
    }

    @Transactional
    public AppUserDto registerClient(RegisterRequest request) {
        lookupService.validateEmailNotTaken(request.email());

        Role clientRole = findRoleByName(RoleCodes.CLIENT.name());

        AuthUser authUser = createAuthUser(request.email(), request.password(), clientRole);
        AppUser appUser = createAppUser(request);

        authUser.setAppUser(appUser);
        appUser.setAuthUser(authUser);

        AppUser savedAppUser = appUserRepository.save(appUser);

        confirmUserService.sendConfirmationEmail(authUser);

        log.info("User registered successfully: email={}, role={}", request.email(), clientRole.getName());
        return appUserMapper.toAppUserDto(savedAppUser);
    }

    @Transactional
    public AppUserDto registerInstructor(RegisterRequest request) {
        lookupService.validateEmailNotTaken(request.email());

        Role instructorRole = findRoleByName(RoleCodes.INSTRUCTOR.name());

        AuthUser authUser = createAuthUser(request.email(), request.password(), instructorRole);
        AppUser appUser = createAppUser(request);

        authUser.setAppUser(appUser);
        appUser.setAuthUser(authUser);

        AppUser savedAppUser = appUserRepository.save(appUser);

        // todo: Admin approval step instead of immediate confirmation email
        log.info("Instructor registered successfully: email={}, role={}", request.email(), instructorRole.getName());
        return appUserMapper.toAppUserDto(savedAppUser);
    }


    private AuthUser createAuthUser(String email, String password, Role role) {
        AuthUser authUser = new AuthUser();
        authUser.setEmail(email);
        authUser.setPassword(passwordEncoder.encode(password));
        authUser.setRoles(Set.of(role));
        authUser.setStatus(AuthUserStatus.PENDING);
        authUser.setEnabled(false);

        return authUser;
    }

    private AppUser createAppUser(RegisterRequest request) {
        AppUser user = new AppUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setPhone(request.phone());
        return user;
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
        String token = jwtUtils.extractTokenFromRequest(request)
                .orElseThrow(() -> new ApiException("Invalid or missing token",
                        HttpStatus.UNAUTHORIZED,
                        AuthCodes.INVALID_TOKEN.name()));

        if (!jwtUtils.validateToken(token)) {
            throw new ApiException("Invalid or expired token",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.INVALID_TOKEN.name());
        }

        String email = jwtUtils.extractEmail(token);

        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()));

        AppUser appUser = appUserRepository.findByAuthUser(authUser)
                .orElseThrow(() -> new ApiException("User profile not found",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()));

        return appUserMapper.toAppUserResponseDto(appUser);
    }
}