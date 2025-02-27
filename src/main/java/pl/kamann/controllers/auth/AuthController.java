package pl.kamann.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.AppUserDto;
import pl.kamann.dtos.ForgotPasswordDto;
import pl.kamann.dtos.login.LoginRequest;
import pl.kamann.dtos.login.LoginResponse;
import pl.kamann.dtos.register.RegisterRequest;
import pl.kamann.mappers.AppUserMapper;
import pl.kamann.services.AuthService;
import pl.kamann.services.ConfirmUserService;
import pl.kamann.services.PasswordResetService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "1. login", description = "Auth controller")
public class AuthController {

    private final ConfirmUserService confirmUserService;
    private final AuthService authService;
    private final AppUserMapper appUserMapper;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Registers a new user.")
    public ResponseEntity<AppUserDto> register(@RequestBody @Valid RegisterRequest request) {
        AppUserDto response = appUserMapper.toDto(authService.registerUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/confirm")
    @Operation(
            summary = "Confirm a user account",
            description = "Confirm a user account by providing the confirmation token. This endpoint requires the token as a query parameter."
    )
    public ResponseEntity<String> confirmUserAccount(@RequestParam("token") String token) {
        confirmUserService.confirmUserAccount(token);
        return ResponseEntity.ok("Your account has been confirmed.");
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot password",
            description = "Send a password reset link to the provided email address."
    )
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        passwordResetService.forgotPassword(forgotPasswordDto.email());
        return ResponseEntity.ok("Password reset link has been sent to your email address.");

    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Reset the password for the user account."
    )
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestBody String password) {
        passwordResetService.resetPassword(token, password);
        return ResponseEntity.ok("Password has been reset.");
    }
}