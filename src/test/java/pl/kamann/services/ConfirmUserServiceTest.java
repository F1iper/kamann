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
import pl.kamann.entities.appuser.AppUserTokens;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.repositories.AppUserRepository;

import java.util.Date;

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
        AppUserTokens tokens = new AppUserTokens();
        tokens.setConfirmationToken("test_token");
        tokens.setExpirationDate(new Date(System.currentTimeMillis() + 100000));

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setAppUserTokens(tokens);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        tokens.setAppUser(user);

        appUserRepository.save(user);

        confirmUserService.confirmUserAccount("test_token");

        AppUser updatedUser = appUserRepository.findByEmail("test@test.com").orElseThrow();
        AppUserTokens updatedTokens = updatedUser.getAppUserTokens();

        assertNull(updatedTokens);
        assertTrue(updatedUser.isEnabled());
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
        AppUserTokens tokens = new AppUserTokens();
        tokens.setConfirmationToken("test_token");
        tokens.setExpirationDate(new Date(System.currentTimeMillis() + 100000));

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setAppUserTokens(tokens);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");
        user.setEnabled(true);

        tokens.setAppUser(user);

        appUserRepository.save(user);

        confirmUserService.confirmUserAccount("test_token");

        AppUser updatedUser = appUserRepository.findByEmail("test@test.com").orElseThrow();
        AppUserTokens updatedTokens = updatedUser.getAppUserTokens();
        assertTrue(updatedUser.isEnabled());
        assertNull(updatedTokens);
    }



    @Test
    void shouldSendConfirmationEmailAfterActivation() throws MessagingException {
        AppUserTokens tokens = new AppUserTokens();
        tokens.setConfirmationToken("test_token");

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setAppUserTokens(tokens);
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
