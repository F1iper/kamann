package pl.kamann.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailSender implements EmailSenderFacade {
    private final JavaMailSender javaMailSender;

    public void sendEmail(String to, String link, Locale userLocale, String type) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        EmailContentBuilder emailContentBuilder = new EmailContentBuilder(userLocale, type);

        helper.setTo(to);
        helper.setSubject(emailContentBuilder.getSubject(type));
        helper.setText(emailContentBuilder.buildConfirmationEmail(link), true);

        javaMailSender.send(message);
    }
}
