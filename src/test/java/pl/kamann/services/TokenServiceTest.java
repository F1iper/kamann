package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import pl.kamann.entities.appuser.AppUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateToken() {
        String token = tokenService.generateConfirmationToken();
        assertEquals(36, token.length());
    }

    @Test
    void shouldGenerateConfirmationLink() {
        AppUser user = new AppUser();
        user.setConfirmationToken("test_token");

        String confirmationLink = tokenService.generateConfirmationLink(user.getConfirmationToken(), "http://localhost:8080/users/confirm?token=");

        assertEquals("http://localhost:8080/users/confirm?token=test_token", confirmationLink);
    }
}
