package pl.kamann.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserTokens;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AppUserTokensRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final AppUserRepository appUserRepository;
    private final TokenService tokenService;
    private final AppUserTokensRepository appUserTokensRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    public void forgotPassword(String email) {
        log.info("Forgot password request for email: {}", email);
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User with email: " + email + " not found.",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()
                ));

        AppUserTokens tokens = new AppUserTokens();

        tokens.setAppUser(appUser);
        tokens.setResetPasswordToken(tokenService.generateToken());
        tokens.setExpirationDate(tokenService.generateExpirationDate());
        appUser.setAppUserTokens(tokens);

        try {
            log.info("Sending reset password email to: {}", email);
            emailSender.sendEmail(appUser.getEmail(), tokenService.generateResetPasswordLink(tokens.getResetPasswordToken(), tokenService.getResetPasswordLink()), Locale.ENGLISH, "reset.password");
        } catch (MessagingException e) {
            log.error("Error sending the reset password email: {}", e.getMessage());
            throw new ApiException(
                    "Error sending the reset password email.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthCodes.RESET_PASSWORD_EMAIL_ERROR.name()
            );
        }

        appUserTokensRepository.save(tokens);
    }

    public void resetPassword(String token, String password) {
        log.info("Reset password request for token: {}", token);
        AppUserTokens appUserTokens = appUserTokensRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new ApiException(
                        "Invalid reset password token.",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.INVALID_RESET_PASSWORD_TOKEN.name()
                ));

        if (appUserTokens.isTokenExpired()) {
            log.warn("Reset password token expired: {}", token);
            throw new ApiException(
                    "Reset password token expired.",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.RESET_PASSWORD_TOKEN_EXPIRED.name()
            );
        }

        AppUser appUser = appUserTokens.getAppUser();
        appUser.setPassword(passwordEncoder.encode(password));
        appUserRepository.save(appUser);

        appUser.setAppUserTokens(null);
        appUserTokensRepository.save(appUserTokens);
        log.info("Password reset successfully for email: {}", appUser.getEmail());
    }
}
