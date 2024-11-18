package pl.kamann.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.auth.login.request.LoginRequest;
import pl.kamann.auth.login.response.LoginResponse;
import pl.kamann.auth.register.RegisterRequest;
import pl.kamann.security.jwt.JwtUtils;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.repository.AppUserRepository;

import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserRepository appUserRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<AppUser> user = appUserRepository.findByEmail(request.email());
        if (user.isPresent() && passwordEncoder.matches(request.password(), user.get().getPassword())) {
            String token = jwtUtils.generateToken(user.get().getEmail());
            return ResponseEntity.ok(new LoginResponse(token));
        }
        log.warn("Failed login attempt for email: {}", request.email());
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (appUserRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(400).body("Email already in use");
        }

        // Create and save the new user
        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setPassword(passwordEncoder.encode(request.password()));
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());
        appUser.setRoles(Set.of("ADMIN"));
        appUserRepository.save(appUser);

        return ResponseEntity.ok("User registered successfully");
    }
}
