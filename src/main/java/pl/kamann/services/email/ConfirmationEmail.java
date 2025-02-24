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
public class ConfirmationEmail implements ConfirmationEmailFacade{
    private final JavaMailSender javaMailSender;

    public void sendConfirmationEmail(String to, String confirmationLink, Locale userLocale) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        EmailContentBuilder emailContentBuilder = new EmailContentBuilder(userLocale);

        helper.setTo(to);
        helper.setSubject(emailContentBuilder.getSubject());
        helper.setText(emailContentBuilder.buildConfirmationEmail(confirmationLink), true);

        javaMailSender.send(message);
    }
}
