package pl.kamann.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.auth.login.request.LoginRequest;
import pl.kamann.auth.login.response.LoginResponse;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.auth.service.AuthService;
import pl.kamann.security.jwt.JwtUtils;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserRepository appUserRepository;
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<AppUser> user = appUserRepository.findByEmail(request.email());
        if (user.isPresent() && passwordEncoder.matches(request.password(), user.get().getPassword())) {
            String token = jwtUtils.generateToken(user.get().getEmail(), user.get().getRoles());
            return ResponseEntity.ok(new LoginResponse(token));
        }
        log.warn("Failed login attempt for email: {}", request.email());
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register/client")
    public ResponseEntity<?> registerClient(@RequestBody RegisterRequest request) {
        authService.registerClient(request);
        return ResponseEntity.ok("Client registered successfully");
    }

    @PostMapping("/register/instructor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerInstructor(@RequestBody RegisterRequest request) {
        authService.registerInstructor(request);
        return ResponseEntity.ok("Instructor registered successfully");
    }
}
