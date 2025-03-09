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
import pl.kamann.dtos.ResetPasswordRequest;
import pl.kamann.dtos.login.LoginRequest;
import pl.kamann.dtos.login.LoginResponse;
import pl.kamann.dtos.register.RegisterRequest;
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
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }

    @PostMapping("/register-client")
    @Operation(summary = "Client Registration", description = "Registers a new client.")
    public ResponseEntity<AppUserDto> registerClient(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(request));
    }

    @PostMapping("/register-instructor")
    @Operation(summary = "Instructor Registration", description = "Registers a new instructor.")
    public ResponseEntity<AppUserDto> registerInstructor(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerInstructor(request));
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

    @PostMapping("/request-password-reset")
    @Operation(summary = "Request Password Reset", description = "Send reset password email to user.")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok("If an account exists with that email, a password reset email has been sent.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Reset the password using a reset token.")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPasswordWithToken(request);
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}