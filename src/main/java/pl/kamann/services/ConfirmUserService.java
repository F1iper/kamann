package pl.kamann.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.ConfirmationEmail;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmUserService {
    private final AppUserRepository appUserRepository;
    private final ConfirmationEmail emailSender;
    private final TokenService tokenService;

    public void sendConfirmationEmail(AppUser appUser) {
        try {
            emailSender.sendConfirmationEmail(appUser.getEmail(), tokenService.generateConfirmationLink(appUser.getConfirmationToken(), tokenService.getConfirmationLink()), Locale.ENGLISH);
            log.info("Confirmation email sent successfully to user: {}", appUser.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending the confirmation email to user: {}", appUser.getEmail(), e);
            throw new ApiException(
                    "Error sending the confirmation email.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.name()
            );
        }
    }

    public void confirmUserAccount(String token) {
        appUserRepository.findByConfirmationToken(token)
                .ifPresentOrElse(
                        user -> {
                            user.setConfirmationToken(null);
                            user.setEnabled(true);
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
