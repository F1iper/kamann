package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamann.entities.appuser.AppUserTokens;

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
        tokens.setConfirmationToken("test_token");
        tokens.setExpirationDate(tokenService.generateExpirationDate());

        long difference = tokens.getExpirationDate().getTime() - System.currentTimeMillis();
        String confirmationLink = tokenService.generateConfirmationLink(tokens.getConfirmationToken(), "http://localhost:8080/api/auth/confirm?token=");

        assertEquals("http://localhost:8080/api/auth/confirm?token=test_token", confirmationLink);
        assertEquals(600000, difference);
    }

    @Test
    void shouldGenerateResetPasswordLink() {
        AppUserTokens tokens = new AppUserTokens();
        tokens.setResetPasswordToken("test_token");
        tokens.setExpirationDate(tokenService.generateExpirationDate());

        long difference = tokens.getExpirationDate().getTime() - System.currentTimeMillis();
        String resetPasswordLink = tokenService.generateResetPasswordLink(tokens.getResetPasswordToken(), "http://localhost:8080/api/auth/reset-password?token=");

        assertEquals("http://localhost:8080/api/auth/reset-password?token=test_token", resetPasswordLink);
        assertEquals(600000, difference);
    }

    @Test
    void shouldGenerateExpirationDate() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = tokenService.generateExpirationDate().getTime();
        long difference = expirationTime - currentTime;

        assertEquals(600000, difference);
    }
}
