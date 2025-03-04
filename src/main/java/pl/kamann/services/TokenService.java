package pl.kamann.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.entities.appuser.TokenType;

@Service
@Getter
@RequiredArgsConstructor
public class TokenService {
    @Value("${confirmation.link}")
    private String confirmationLink;

    @Value("${reset.password.link}")
    private String resetPasswordLink;

    private final JwtUtils jwtUtils;

    public String generateConfirmationLink(String token, String confirmationLink) {
        return confirmationLink + token;
    }

    public String generateResetPasswordLink(String token, String resetPasswordLink) {
        return resetPasswordLink + token;
    }

    public String generateToken(String email, TokenType tokenType, long expirationTime) {
        return jwtUtils.generateTokenWithFlag(email, tokenType, expirationTime);
    }
}
