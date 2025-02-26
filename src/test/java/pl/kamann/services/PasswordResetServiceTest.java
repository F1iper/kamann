package pl.kamann.services;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AppUserTokens;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ContextConfiguration(classes = TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional
public class PasswordResetServiceTest {

    @MockBean
    private EmailSender emailSender;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldForgotPassword() {

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        appUserRepository.save(user);

        passwordResetService.forgotPassword(user.getEmail());

        AppUserTokens updatedTokens = appUserRepository.findByEmail(user.getEmail()).orElseThrow().getAppUserTokens();

        assertNotNull(updatedTokens);
    }

    @Test
    void shouldResetPassword() {
        AppUserTokens tokens = new AppUserTokens();
        tokens.setResetPasswordToken("test_token");
        tokens.setExpirationDate(new Date(System.currentTimeMillis() + 100000));

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setAppUserTokens(tokens);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        tokens.setAppUser(user);

        appUserRepository.save(user);

        passwordResetService.resetPassword("test_token", "new_password");

        AppUser updatedUser = appUserRepository.findByEmail(user.getEmail()).orElseThrow();
        AppUserTokens updatedTokens = updatedUser.getAppUserTokens();

        assertNull(updatedTokens);
        assertTrue(passwordEncoder.matches("new_password", updatedUser.getPassword()));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                passwordResetService.resetPassword("invalid_token", "new_password")
        );
        assertTrue(exception.getMessage().contains("Invalid reset password token"));
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        AppUserTokens tokens = new AppUserTokens();
        tokens.setResetPasswordToken("test_token");
        tokens.setExpirationDate(new Date(System.currentTimeMillis() - 100000));

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setAppUserTokens(tokens);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        tokens.setAppUser(user);

        appUserRepository.save(user);

        Exception exception = assertThrows(RuntimeException.class, () ->
                passwordResetService.resetPassword("test_token", "new_password")
        );

        assertTrue(exception.getMessage().contains("Reset password token expired"));
    }

    @Test
    void shouldThrowExceptionForUserNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                passwordResetService.forgotPassword("")
        );

        assertTrue(exception.getMessage().contains("User with email:  not found"));
    }

    @Test
    void shouldThrowExceptionForErrorSendingEmail() throws MessagingException {
        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        appUserRepository.save(user);

        doThrow(new RuntimeException("Error sending the reset password email")).when(emailSender).sendEmail(anyString(), anyString(), any(Locale.class), anyString());

        Exception exception = assertThrows(RuntimeException.class, () ->
                passwordResetService.forgotPassword(user.getEmail())
        );

        assertTrue(exception.getMessage().contains("Error sending the reset password email"));
    }
}
