package pl.kamann.services.email;

import jakarta.mail.MessagingException;

import java.util.Locale;

public interface ConfirmationEmailFacade {
    void sendConfirmationEmail(String to, String confirmationLink, Locale userLocale)throws MessagingException;
}
