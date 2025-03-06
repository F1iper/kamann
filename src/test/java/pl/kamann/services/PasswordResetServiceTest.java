package pl.kamann.services;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.ResetPasswordRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional
public class PasswordResetServiceTest {

    @MockBean
    private EmailSender emailSender;


    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRequestPasswordReset() {
        AppUser user = new AppUser();
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setEmail("test@test.com");
        user.setPassword("old_Password");
        user.setEnabled(true);

        appUserRepository.save(user);
    }

    @Test
    void shouldResetPasswordWithToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("test_token");
        request.setNewPassword("new_password");

        String email = "test@test.com";
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode("old_password"));

        appUserRepository.save(user);

        when(jwtUtils.validateToken(request.getToken())).thenReturn(true);
        when(jwtUtils.isTokenTypeValid(request.getToken(), TokenType.RESET_PASSWORD)).thenReturn(true);
        when(jwtUtils.extractEmail(request.getToken())).thenReturn(email);

        passwordResetService.resetPasswordWithToken(request);

        AppUser updatedUser = appUserRepository.findByEmail(user.getEmail()).orElseThrow();
        assertTrue(passwordEncoder.matches("new_password", updatedUser.getPassword()), "Password should be updated");
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
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("test_token");
        request.setNewPassword("new_password");

        AppUser user = new AppUser();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashed_password");

        appUserRepository.save(user);

        ApiException exception = assertThrows(ApiException.class, () ->
                passwordResetService.resetPasswordWithToken(request)
        );
        assertTrue(exception.getMessage().contains("Invalid reset password token."));
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

        doThrow(new ApiException("Error sending the reset password email", HttpStatus.INTERNAL_SERVER_ERROR, AuthCodes.RESET_PASSWORD_EMAIL_ERROR.name()))
                .when(emailSender).sendEmail(anyString(), anyString(), any(Locale.class), anyString());

        assertThrows(ApiException.class, () ->
                passwordResetService.requestPasswordReset(user.getEmail())
        );
    }
}