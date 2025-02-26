package pl.kamann.services.email;

import jakarta.mail.MessagingException;

import java.util.Locale;

public interface EmailSenderFacade {
    void sendEmail(String to, String confirmationLink, Locale userLocale, String type)throws MessagingException;
}
