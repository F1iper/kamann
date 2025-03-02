package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamann.entities.appuser.Token;
import pl.kamann.entities.appuser.TokenType;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateToken() {
        String token = tokenService.generateToken();
        assertEquals(36, token.length());
    }

    @Test
    void shouldGenerateConfirmationLink() {
        Token tokens = new Token();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.CONFIRMATION);
        tokens.setExpirationDate(tokenService.generateExpirationDate());

        long difference = Duration.between(LocalDateTime.now(), tokens.getExpirationDate()).toMillis();
        String confirmationLink = tokenService.generateConfirmationLink(tokens.getToken(), "http://localhost:8080/api/auth/confirm?token=");

        assertEquals("http://localhost:8080/api/auth/confirm?token=test_token", confirmationLink);
        assertTrue(Math.abs(difference - 600000) <= 5, "Expiration time should be close to 600000 ms but was: " + difference);
    }

    @Test
    void shouldGenerateResetPasswordLink() {
        Token tokens = new Token();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.RESET_PASSWORD);
        tokens.setExpirationDate(tokenService.generateExpirationDate());

        long difference = Duration.between(LocalDateTime.now(), tokens.getExpirationDate()).toMillis();
        String resetPasswordLink = tokenService.generateResetPasswordLink(tokens.getToken(), "http://localhost:8080/api/auth/reset-password?token=");

        assertEquals("http://localhost:8080/api/auth/reset-password?token=test_token", resetPasswordLink);
        assertTrue(Math.abs(difference - 600000) <= 5, "Expiration time should be close to 600000 ms but was: " + difference);
    }

    @Test
    void shouldGenerateExpirationDate() {
        long difference = Duration.between(LocalDateTime.now(), tokenService.generateExpirationDate()).toMillis();

        assertTrue(Math.abs(difference - 600000) <= 5, "Expiration time should be close to 600000 ms but was: " + difference);
    }
}
