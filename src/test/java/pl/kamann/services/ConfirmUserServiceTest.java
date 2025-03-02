package pl.kamann.services;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Token;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional
public class ConfirmUserServiceTest {

    @MockBean
    private EmailSender emailSender;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ConfirmUserService confirmUserService;

    @Test
    void shouldConfirmAccount() {
        Token tokens = new Token();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.CONFIRMATION);
        tokens.setExpirationDate(LocalDateTime.now().plusMinutes(10));

        Set<Token> tokenSet = new HashSet<>();
        tokenSet.add(tokens);

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setTokens(tokenSet);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        tokens.setAppUser(user);

        appUserRepository.save(user);

        confirmUserService.confirmUserAccount("test_token");

        AppUser updatedUser = appUserRepository.findByEmail("test@test.com").orElseThrow();
        Set<Token> updatedTokens = updatedUser.getTokens();

        assertTrue(updatedUser.isEnabled());
        assertTrue(updatedTokens.isEmpty(), "Token should be removed after confirmation");
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                confirmUserService.confirmUserAccount("invalid_token")
        );
        assertTrue(exception.getMessage().contains("Invalid confirmation token"));
    }

    @Test
    void shouldNotConfirmAlreadyConfirmedAccount() {
        Token tokens = new Token();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.CONFIRMATION);
        tokens.setExpirationDate(LocalDateTime.now().plusMinutes(10));

        Set<Token> tokenSet = new HashSet<>();
        tokenSet.add(tokens);

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setTokens(tokenSet);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");
        user.setEnabled(true);

        tokens.setAppUser(user);

        appUserRepository.save(user);

        confirmUserService.confirmUserAccount("test_token");

        AppUser updatedUser = appUserRepository.findByEmail("test@test.com").orElseThrow();
        Set<Token> updatedTokens = updatedUser.getTokens();
        assertTrue(updatedUser.isEnabled(), "User should still be enabled");
        assertTrue(updatedTokens.isEmpty(), "Token should be removed after confirmation");
    }

    @Test
    void shouldSendConfirmationEmailAfterActivation() throws MessagingException {
        Token tokens = new Token();
        tokens.setToken("test_token");
        tokens.setTokenType(TokenType.CONFIRMATION);
        tokens.setExpirationDate(LocalDateTime.now().plusMinutes(10));

        Set<Token> tokenSet = new HashSet<>();
        tokenSet.add(tokens);

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setTokens(tokenSet);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        tokens.setAppUser(user);

        appUserRepository.save(user);

        confirmUserService.sendConfirmationEmail(user);

        Mockito.verify(emailSender, Mockito.times(1))
                .sendEmail(Mockito.eq("test@test.com"), Mockito.anyString(), Mockito.any(), Mockito.eq("registration"));
    }
}