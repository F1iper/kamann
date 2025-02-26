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

        String confirmationLink = tokenService.generateConfirmationLink(tokens.getConfirmationToken(), "http://localhost:8080/users/confirm?token=");

        assertEquals("http://localhost:8080/users/confirm?token=test_token", confirmationLink);
    }
}
