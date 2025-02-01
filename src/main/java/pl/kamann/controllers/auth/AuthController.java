package pl.kamann.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamann.dtos.login.LoginRequest;
import pl.kamann.dtos.login.LoginResponse;
import pl.kamann.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "/api/auth", description = "Auth controller")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}