package pl.kamann.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.dtos.LoginRequest;
import pl.kamann.dtos.LoginResponse;
import pl.kamann.dtos.RegisterRequest;
import pl.kamann.services.AuthService;
import pl.kamann.entities.AppUser;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register user by ADMIN with Role assigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUser> registerUser(@RequestBody RegisterRequest request) {
        AppUser registeredUser = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
}