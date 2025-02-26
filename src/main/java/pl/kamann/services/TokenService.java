package pl.kamann.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
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
        return UUID.randomUUID().toString();
    }

    public Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + 1000 * 60 * 10);
    }
}
