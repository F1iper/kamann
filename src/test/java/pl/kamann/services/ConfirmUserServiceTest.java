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
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        appUserRepository.save(user);

        confirmUserService.confirmUserAccount("test_token");

        AppUser updatedUser = appUserRepository.findByEmail("test@test.com").orElseThrow();

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
        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");
        user.setEnabled(true);

        appUserRepository.save(user);

        confirmUserService.confirmUserAccount("test_token");

        AppUser updatedUser = appUserRepository.findByEmail("test@test.com").orElseThrow();
        assertTrue(updatedUser.isEnabled(), "User should still be enabled");
    }

    @Test
    void shouldSendConfirmationEmailAfterActivation() throws MessagingException {

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        appUserRepository.save(user);

        confirmUserService.sendConfirmationEmail(user);

        Mockito.verify(emailSender, Mockito.times(1))
                .sendEmail(Mockito.eq("test@test.com"), Mockito.anyString(), Mockito.any(), Mockito.eq("registration"));
    }
}