package pl.kamann.services;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.ResetPasswordRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Token;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
    void shouldRequestPasswordReset() {
        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");
        appUserRepository.save(user);

        passwordResetService.requestPasswordReset(user.getEmail());

        Set<Token> updatedTokens = appUserRepository.findByEmail(user.getEmail()).orElseThrow().getTokens();
        assertNotNull(updatedTokens);
        assertFalse(updatedTokens.isEmpty());
    }

    @Test
    void shouldResetPasswordWithToken() {
        Token token = new Token();
        token.setToken("test_token");
        token.setTokenType(TokenType.RESET_PASSWORD);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("test_token");
        request.setNewPassword("new_password");

        Set<Token> tokenSet = new HashSet<>();
        tokenSet.add(token);

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setTokens(tokenSet);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode("old_password"));

        token.setAppUser(user);
        appUserRepository.save(user);

        passwordResetService.resetPasswordWithToken(request);

        AppUser updatedUser = appUserRepository.findByEmail(user.getEmail()).orElseThrow();
        Token updatedToken = updatedUser.getTokens().stream()
                .filter(t -> t.getToken().equals("test_token"))
                .findFirst()
                .orElse(null);

        assertNull(updatedToken, "Token should be deleted after password reset.");
        assertTrue(passwordEncoder.matches("new_password", updatedUser.getPassword()));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid_token");
        request.setNewPassword("new_password");

        ApiException exception = assertThrows(ApiException.class, () ->
                passwordResetService.resetPasswordWithToken(request)
        );
        assertTrue(exception.getMessage().contains("Invalid reset password token"));
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        Token token = new Token();
        token.setToken("test_token");
        token.setTokenType(TokenType.RESET_PASSWORD);
        token.setExpirationDate(LocalDateTime.now().minusMinutes(10)); // Expired

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("test_token");
        request.setNewPassword("new_password");

        Set<Token> tokenSet = new HashSet<>();
        tokenSet.add(token);

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setTokens(tokenSet);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        token.setAppUser(user);
        appUserRepository.save(user);

        ApiException exception = assertThrows(ApiException.class, () ->
                passwordResetService.resetPasswordWithToken(request)
        );
        assertTrue(exception.getMessage().contains("Reset password token expired"));
    }

    @Test
    void shouldThrowExceptionForUserNotFound() {
        ApiException exception = assertThrows(ApiException.class, () ->
                passwordResetService.requestPasswordReset("nonexistent@test.com")
        );
        assertTrue(exception.getMessage().contains("User with email: nonexistent@test.com not found"));
    }

    @Test
    void shouldThrowExceptionForErrorSendingEmail() throws MessagingException {
        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");
        appUserRepository.save(user);

        doThrow(new RuntimeException("Error sending the reset password email"))
                .when(emailSender).sendEmail(anyString(), anyString(), any(Locale.class), anyString());

        ApiException exception = assertThrows(ApiException.class, () ->
                passwordResetService.requestPasswordReset(user.getEmail())
        );
        assertTrue(exception.getMessage().contains("Error sending the reset password email"));
    }
}
