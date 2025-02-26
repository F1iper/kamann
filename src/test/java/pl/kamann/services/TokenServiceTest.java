package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamann.entities.appuser.AppUserTokens;
import pl.kamann.entities.appuser.TokenType;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        AppUserTokens tokens = new AppUserTokens();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.CONFIRMATION);
        tokens.setExpirationDate(tokenService.generateExpirationDate());

        long difference = Duration.between(LocalDateTime.now(), tokens.getExpirationDate()).toMillis();
        String confirmationLink = tokenService.generateConfirmationLink(tokens.getToken(), "http://localhost:8080/api/auth/confirm?token=");

        assertEquals("http://localhost:8080/api/auth/confirm?token=test_token", confirmationLink);
        assertEquals(600000, difference);
    }

    @Test
    void shouldGenerateResetPasswordLink() {
        AppUserTokens tokens = new AppUserTokens();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.RESET_PASSWORD);
        tokens.setExpirationDate(tokenService.generateExpirationDate());

        long difference = Duration.between(LocalDateTime.now(), tokens.getExpirationDate()).toMillis();
        String resetPasswordLink = tokenService.generateResetPasswordLink(tokens.getToken(), "http://localhost:8080/api/auth/reset-password?token=");

        assertEquals("http://localhost:8080/api/auth/reset-password?token=test_token", resetPasswordLink);
        assertEquals(600000, difference);
    }

    @Test
    void shouldGenerateExpirationDate() {
        long difference = Duration.between(LocalDateTime.now(), tokenService.generateExpirationDate()).toMillis();

        assertEquals(600000, difference);
    }
}
