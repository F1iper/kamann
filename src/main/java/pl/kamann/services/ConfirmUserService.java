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
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AppUserTokensRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmUserService {
    private final AppUserRepository appUserRepository;
    private final EmailSender emailSender;
    private final TokenService tokenService;
    private final AppUserTokensRepository appUserTokensRepository;

    public void sendConfirmationEmail(AppUser appUser) {
        try {
            emailSender.sendEmail(appUser.getEmail(), tokenService.generateConfirmationLink(appUser.getAppUserTokens().getConfirmationToken(), tokenService.getConfirmationLink()), Locale.ENGLISH, "registration");
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

    public void confirmUserAccount(String token) {
        appUserRepository.findByConfirmationToken(token)
                .ifPresentOrElse(
                        user -> {
                            user.setEnabled(true);
                            AppUserTokens tokens = user.getAppUserTokens();
                            if (tokens != null && !tokens.isTokenExpired()) {
                                user.setAppUserTokens(null);
                                appUserTokensRepository.save(tokens);
                            }

                            appUserRepository.save(user);

                            log.info("User account confirmed for: {}", user.getEmail());
                        },
                        () -> {
                            log.error("Invalid confirmation token: {}", token);
                            throw new ApiException(
                                    "Invalid confirmation token.",
                                    HttpStatus.NOT_FOUND,
                                    AuthCodes.INVALID_CONFIRMATION_TOKEN.name()
                            );
                        }
                );
    }
}
