package pl.kamann.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Getter
public class TokenService {
    @Value("${confirmation.link}")
    private String confirmationLink;

    public String generateConfirmationLink(String token, String confirmationLink) {
        return confirmationLink + token;
    }

    public String generateConfirmationToken() {
        return UUID.randomUUID().toString();
    }
}
