package pl.kamann.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.auth.login.request.LoginRequest;
import pl.kamann.auth.login.response.LoginResponse;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.auth.service.AuthService;
import pl.kamann.exception.response.ErrorResponse;
import pl.kamann.global.Codes;
import pl.kamann.security.jwt.JwtUtils;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDateTime;

import static pl.kamann.global.Codes.INVALID_CREDENTIALS;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), INVALID_CREDENTIALS, "Invalid password", LocalDateTime.now()));
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRoles());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register/client")
    @Operation(summary = "Register Client", description = "Registers a user with the default CLIENT role.")
    public ResponseEntity<?> registerClient(@RequestBody @Valid RegisterRequest request) {
        authService.registerClient(request);
        return ResponseEntity.ok(new ErrorResponse(HttpStatus.OK.value(), Codes.SUCCESS, "Client registered successfully", LocalDateTime.now()));
    }

    @PostMapping("/register/instructor")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register Instructor", description = "Registers a user with the INSTRUCTOR role. Requires ADMIN privileges.")
    public ResponseEntity<?> registerInstructor(@RequestBody @Valid RegisterRequest request) {
        authService.registerInstructor(request);
        return ResponseEntity.ok(new ErrorResponse(HttpStatus.OK.value(), Codes.SUCCESS, "Instructor registered successfully", LocalDateTime.now()));
    }
}