package pl.kamann.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.ResetPasswordRequest;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AuthUserRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final AuthUserRepository authUserRepository;
    private final TokenService tokenService;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        AuthUser authUser = validateUserForReset(email);

        sendResetPasswordEmail(authUser);

        log.info("Reset password email sent successfully to: {}", email);
    }

    private AuthUser validateUserForReset(String email) {
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset attempt for non-existent email: {}", email);
                    return new ApiException(
                            "User with email: " + email + " not found.",
                            HttpStatus.NOT_FOUND,
                            AuthCodes.USER_NOT_FOUND.name()
                    );
                });

        if (!authUser.isEnabled()) {
            log.warn("Password reset requested for a disabled user: {}", email);
            throw new ApiException(
                    "Your account is not active. Please contact support.",
                    HttpStatus.FORBIDDEN,
                    AuthCodes.USER_NOT_ACTIVE.name()
            );
        }
        return authUser;
    }

    private void sendResetPasswordEmail(AuthUser authUser) {
        String token = tokenService.generateToken(authUser.getEmail(), TokenType.RESET_PASSWORD, 15 * 60 * 1000);

        try {
            String resetLink = tokenService.generateResetPasswordLink(token, tokenService.getResetPasswordLink());
            log.info("Sending reset password email to: {}", authUser.getEmail());
            emailSender.sendEmail(authUser.getEmail(), resetLink, Locale.ENGLISH, "reset.password");
        } catch (MessagingException e) {
            log.error("Error sending reset password email to {}: {}", authUser.getEmail(), e.getMessage(), e);
            throw new ApiException(
                    "Error sending the reset password email.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthCodes.RESET_PASSWORD_EMAIL_ERROR.name()
            );
        }
    }

    @Transactional
    public void resetPasswordWithToken(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        log.info("Reset password attempt for token: {}", token);

        if(jwtUtils.validateToken(token) && jwtUtils.isTokenTypeValid(token, TokenType.RESET_PASSWORD)) {
            String email = jwtUtils.extractEmail(token);

            AuthUser authUser = authUserRepository.findByEmail(email).orElseThrow(() ->
                    new ApiException(
                            "User not found",
                            HttpStatus.NOT_FOUND,
                            AuthCodes.USER_NOT_FOUND.name()
                    )
            );

            authUser.setPassword(passwordEncoder.encode(newPassword));
            authUserRepository.save(authUser);

            log.info("Password reset successfully for email: {}", authUser.getEmail());
        } else {
            throw new ApiException(
                    "Invalid reset password token.",
                    HttpStatus.NOT_FOUND,
                    AuthCodes.INVALID_TOKEN.name()
            );
        }
    }
}