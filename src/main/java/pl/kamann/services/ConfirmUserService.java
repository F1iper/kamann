package pl.kamann.services;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserStatus;
import pl.kamann.entities.appuser.Token;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.TokenRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmUserService {
    private final AppUserRepository appUserRepository;
    private final EmailSender emailSender;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;

    public void sendConfirmationEmail(AppUser appUser) {
        Set<Token> tokens = appUser.getTokens();
        Token confirmationToken = tokens.stream()
                .filter(t -> t.getTokenType().equals(TokenType.CONFIRMATION))
                .findFirst()
                .orElse(null);

        if (confirmationToken != null && !confirmationToken.isExpired()) {
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

    @Transactional
    public void confirmUserAccount(String token) {
        log.info("Confirming user account for token: {}", token);
        AppUser user = appUserRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(
                        "Invalid confirmation token.",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.INVALID_TOKEN.name()
                ));

        Token confirmationToken = user.getTokens().stream()
                .filter(t -> t.getToken().equals(token) && t.getTokenType().equals(TokenType.CONFIRMATION) && !t.isExpired())
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        "Confirmation token expired",
                        HttpStatus.BAD_REQUEST,
                        AuthCodes.CONFIRMATION_TOKEN_EXPIRED.name()
                ));
        user.getTokens().remove(confirmationToken);

        tokenRepository.delete(confirmationToken);

        user.setEnabled(true);
        user.setStatus(AppUserStatus.ACTIVE);

        appUserRepository.save(user);

        log.info("User account confirmed for: {}", user.getEmail());
    }
}
