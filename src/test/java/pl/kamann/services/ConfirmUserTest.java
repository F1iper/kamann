package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ConfirmUserTest {
    @InjectMocks
    private ConfirmUser confirmUser;

    @Mock
    private AppUserRepository appUserRepository;

    @Test
    void shouldGenerateToken() {
        String token = confirmUser.generateConfirmationToken();
        assertEquals(36, token.length());
    }

    @Test
    void shouldGenerateConfirmationLink() {
        AppUser user = new AppUser();
        user.setConfirmationToken("test_token");

        String confirmationLink = confirmUser.generateConfirmationLink(user.getConfirmationToken(), "http://localhost:8080/users/confirm?token=");

        assertEquals("http://localhost:8080/users/confirm?token=test_token", confirmationLink);
    }

    @Test
    void shouldConfirmAccount() {
        AppUser user = new AppUser();
        user.setConfirmationToken("test_token");

        when(appUserRepository.findByConfirmationToken("test_token")).thenReturn(Optional.of(user));

        assertEquals("test_token",  user.getConfirmationToken());
        assertFalse(user.isConfirmed());
    }

    @Test
    void isAccountConfirmed() {
        AppUser user = new AppUser();
        user.setConfirmationToken("test_token");

        when(appUserRepository.findByConfirmationToken("test_token")).thenReturn(Optional.of(user));
        confirmUser.confirmUserAccount(user.getConfirmationToken());

        assertNull(user.getConfirmationToken());
        assertTrue(user.isConfirmed());
    }

}
