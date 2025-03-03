package pl.kamann.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Getter
public class TokenService {
    @Value("${confirmation.link}")
    private String confirmationLink;

    @Value("${reset.password.link}")
    private String resetPasswordLink;

    public String generateConfirmationLink(String token, String confirmationLink) {
        return confirmationLink + token;
    }

    public String generateResetPasswordLink(String token, String resetPasswordLink) {
        return resetPasswordLink + token;
    }

    public String generateToken() {
        // todo use JWT
        return UUID.randomUUID().toString();
    }

    public LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusMinutes(10);
    }
}
