package pl.kamann.services;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmUserService {
    private final AppUserRepository appUserRepository;
    private final EmailSender emailSender;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;

    public void sendConfirmationEmail(AppUser appUser) {

        String token = tokenService.generateToken(appUser.getEmail(), TokenType.CONFIRMATION, 15 * 60 * 1000);

        try {
            String confirmationLink = tokenService.generateConfirmationLink(token, tokenService.getConfirmationLink());
            emailSender.sendEmail(appUser.getEmail(), confirmationLink, Locale.ENGLISH, "registration");
            log.info("Confirmation email sent successfully to user: {}", appUser.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending the confirmation email to user: {}", appUser.getEmail(), e);
            throw new ApiException(
                    "Error sending the confirmation email.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthCodes.CONFIRMATION_EMAIL_ERROR.name()
            );
        }
    }

    @Transactional
    public void confirmUserAccount(String token) {
        log.info("Confirming user account for token: {}", token);

        if (!jwtUtils.validateToken(token)) {
            throw new ApiException(
                    "Invalid or expired confirmation Token",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.INVALID_TOKEN.name()
            );
        }

        if (!jwtUtils.isTokenTypeValid(token, TokenType.CONFIRMATION)) {
            throw new ApiException(
                    "Token type is invalid",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.INVALID_TOKEN.name()
            );
        }

        String email = jwtUtils.extractEmail(token);

        AppUser user = appUserRepository.findByEmail(email).orElseThrow(() ->
                new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()
                )
        );

        if (user.isEnabled()) {
            throw new ApiException(
                    "User is already confirmed",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.USER_ALREADY_CONFIRMED.name()
            );
        }

        user.setEnabled(true);
        user.setStatus(AppUserStatus.ACTIVE);

        appUserRepository.save(user);

        log.info("User account confirmed for: {}", user.getEmail());
    }
}
