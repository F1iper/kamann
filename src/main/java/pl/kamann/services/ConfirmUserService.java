package pl.kamann.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserTokens;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AppUserTokensRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmUserService {
    private final AppUserRepository appUserRepository;
    private final EmailSender emailSender;
    private final TokenService tokenService;
    private final AppUserTokensRepository appUserTokensRepository;

    public void sendConfirmationEmail(AppUser appUser) {
        Set<AppUserTokens> tokens = appUser.getAppUserTokens();
        AppUserTokens confirmationToken = tokens.stream()
                .filter(t -> t.getTokenType().equals(TokenType.CONFIRMATION))
                .findFirst()
                .orElse(null);

        if (confirmationToken != null && !confirmationToken.isTokenExpired()) {
            try {
                emailSender.sendEmail(appUser.getEmail(), tokenService.generateConfirmationLink(confirmationToken.getToken(), tokenService.getConfirmationLink()), Locale.ENGLISH, "registration");
                log.info("Confirmation email sent successfully to user: {}", appUser.getEmail());
            } catch (MessagingException e) {
                log.error("Error sending the confirmation email to user: {}", appUser.getEmail(), e);
                throw new ApiException(
                        "Error sending the confirmation email.",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        AuthCodes.CONFIRMATION_EMAIL_ERROR.name()
                );
            }
        } else {
            throw new ApiException(
                    "Confirmation token is invalid or expired.",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.INVALID_TOKEN.name()
            );
        }
    }

    public void confirmUserAccount(String token) {
        log.info("Confirming user account for token: {}", token);
        AppUser user = appUserRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(
                        "Invalid confirmation token.",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.INVALID_TOKEN.name()
                ));

        user.setEnabled(true);

        AppUserTokens confirmationToken = user.getAppUserTokens().stream()
                .filter(t -> t.getToken().equals(token) && t.getTokenType().equals(TokenType.CONFIRMATION) && !t.isTokenExpired())
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        "Confirmation token expired",
                        HttpStatus.BAD_REQUEST,
                        AuthCodes.CONFIRMATION_TOKEN_EXPIRED.name()
                ));

        user.getAppUserTokens().remove(confirmationToken);
        appUserTokensRepository.save(confirmationToken);

        appUserRepository.save(user);

        log.info("User account confirmed for: {}", user.getEmail());
    }
}
