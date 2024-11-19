package pl.kamann.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.time.LocalDateTime;

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

    @PostMapping("/register-client")
    @Operation(summary = "Register Client", description = "Registers a user with the default CLIENT role.")
    public ResponseEntity<?> registerClient(@RequestBody @Valid RegisterRequest request) {
        authService.registerClient(request);
        return ResponseEntity.ok(new ErrorResponse(HttpStatus.OK.value(), Codes.SUCCESS, "Client registered successfully", LocalDateTime.now()));
    }

    @PostMapping("/register-instructor")
    @Operation(summary = "Register Instructor", description = "Registers a user with the INSTRUCTOR role. Requires ADMIN privileges.")
    public ResponseEntity<?> registerInstructor(@RequestBody @Valid RegisterRequest request) {
        authService.registerInstructor(request);
        return ResponseEntity.ok(new ErrorResponse(HttpStatus.OK.value(), Codes.SUCCESS, "Instructor registered successfully", LocalDateTime.now()));
    }
}