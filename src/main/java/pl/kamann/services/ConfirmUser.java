package pl.kamann.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.ConfirmationEmail;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConfirmUser {
    private final AppUserRepository appUserRepository;
    private final ConfirmationEmail emailSender;
    @Value("${confirmation.link}")
    private String confirmationLink;

    public String generateConfirmationLink(String token) {
        return confirmationLink + token;
    }

    public String generateConfirmationToken() {
        return UUID.randomUUID().toString();
    }

    public void sendConfirmationEmail(AppUser appUser) {
        try {
            emailSender.sendConfirmationEmail(appUser.getEmail(), generateConfirmationLink(appUser.getConfirmationToken()));
        } catch (MessagingException e) {
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
                            user.setConfirmed(true);
                            appUserRepository.save(user);
                        },
                        () -> {
                            throw new ApiException(
                                    "Invalid confirmation token.",
                                    HttpStatus.NOT_FOUND,
                                    AuthCodes.INVALID_CONFIRMATION_TOKEN.name()
                            );
                        }
                );
    }
}
