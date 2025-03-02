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
import pl.kamann.dtos.ResetPasswordRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Token;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.TokenRepository;
import pl.kamann.services.email.EmailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final AppUserRepository appUserRepository;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        AppUser appUser = validateUserForReset(email);
        checkExistingResetTokens(appUser);

        Token resetToken = generateResetToken(appUser);
        tokenRepository.save(resetToken);
        appUser.getTokens().add(resetToken);
        appUserRepository.save(appUser);

        sendResetPasswordEmail(appUser, resetToken);

        log.info("Reset password email sent successfully to: {}", email);
    }


    @Transactional
    public void resetPasswordWithToken(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        log.info("Reset password attempt for token: {}", token);

        Token resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid reset password token: {}", token);
                    return new ApiException(
                            "Invalid reset password token.",
                            HttpStatus.NOT_FOUND,
                            AuthCodes.INVALID_TOKEN.name()
                    );
                });

        validateResetToken(resetToken);

        if (!newPassword.equals(confirmPassword)) {
            log.warn("Passwords do not match for reset token: {}", token);
            throw new ApiException(
                    "Passwords do not match.",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.PASSWORDS_DO_NOT_MATCH.name()
            );
        }

        AppUser appUser = resetToken.getAppUser();
        appUser.setPassword(passwordEncoder.encode(newPassword));

        appUser.getTokens().remove(resetToken);
        tokenRepository.delete(resetToken);
        appUserRepository.save(appUser);

        log.info("Password reset successfully for email: {}", appUser.getEmail());
    }

    private AppUser validateUserForReset(String email) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset attempt for non-existent email: {}", email);
                    return new ApiException(
                            "User with email: " + email + " not found.",
                            HttpStatus.NOT_FOUND,
                            AuthCodes.USER_NOT_FOUND.name()
                    );
                });

        if (!appUser.isEnabled()) {
            log.warn("⚠ Password reset requested for a disabled user: {}", email);
            throw new ApiException(
                    "Your account is not active. Please contact support.",
                    HttpStatus.FORBIDDEN,
                    AuthCodes.USER_NOT_ACTIVE.name()
            );
        }
        return appUser;
    }

    private void checkExistingResetTokens(AppUser appUser) {
        List<Token> expiredTokens = new ArrayList<>();
        List<Token> validTokens = new ArrayList<>();

        for (Token token : appUser.getTokens()) {
            if (token.getTokenType().equals(TokenType.RESET_PASSWORD)) {
                if (token.isExpired()) {
                    expiredTokens.add(token);
                } else {
                    validTokens.add(token);
                }
            }
        }

        if (!validTokens.isEmpty()) {
            log.warn("⚠Active reset token already exists for email: {}", appUser.getEmail());
            throw new ApiException(
                    "A reset password token has already been generated. Please check your email.",
                    HttpStatus.CONFLICT,
                    AuthCodes.RESET_PASSWORD_TOKEN_EXISTS.name()
            );
        }

        if (!expiredTokens.isEmpty()) {
            log.warn("Expired reset token found for email: {}", appUser.getEmail());
            throw new ApiException(
                    "Your password reset token has expired. Please request a new one.",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.RESET_PASSWORD_TOKEN_EXPIRED.name()
            );
        }

        tokenRepository.deleteAll(expiredTokens);
        expiredTokens.forEach(appUser.getTokens()::remove);
    }

    private Token generateResetToken(AppUser appUser) {
        Token resetToken = new Token();
        resetToken.setAppUser(appUser);
        resetToken.setToken(tokenService.generateToken());
        resetToken.setTokenType(TokenType.RESET_PASSWORD);
        resetToken.setExpirationDate(tokenService.generateExpirationDate());
        return resetToken;
    }

    private void sendResetPasswordEmail(AppUser appUser, Token resetToken) {
        try {
            String resetLink = tokenService.generateResetPasswordLink(resetToken.getToken(), tokenService.getResetPasswordLink());
            log.info("Sending reset password email to: {}", appUser.getEmail());
            emailSender.sendEmail(appUser.getEmail(), resetLink, Locale.ENGLISH, "reset.password");
        } catch (MessagingException e) {
            log.error("Error sending reset password email to {}: {}", appUser.getEmail(), e.getMessage(), e);
            throw new ApiException(
                    "Error sending the reset password email.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthCodes.RESET_PASSWORD_EMAIL_ERROR.name()
            );
        }
    }

    private void validateResetToken(Token resetToken) {
        if (!resetToken.getTokenType().equals(TokenType.RESET_PASSWORD)) {
            log.warn("Token type mismatch for reset password: {}", resetToken.getToken());
            throw new ApiException(
                    "Invalid token type for password reset.",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.INVALID_TOKEN_TYPE.name()
            );
        }

        if (resetToken.isExpired()) {
            log.warn("Expired reset password token: {}", resetToken.getToken());
            throw new ApiException(
                    "Reset password token expired.",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.RESET_PASSWORD_TOKEN_EXPIRED.name()
            );
        }
    }
}
