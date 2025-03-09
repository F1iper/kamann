package pl.kamann.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.dtos.ResetPasswordRequest;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AuthUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void shouldRequestPasswordReset() throws MessagingException {
        AppUser appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName("Doe");
        appUser.setPhone("123456789");

        AuthUser user = new AuthUser();
        user.setEmail("test@test.com");
        user.setPassword("old_Password");
        user.setEnabled(true);
        user.setAppUser(appUser);

        appUserRepository.save(appUser);
        authUserRepository.save(user);

        doNothing().when(emailSender).sendEmail(anyString(), anyString(), any(Locale.class), anyString());

        passwordResetService.requestPasswordReset(user.getEmail());

        verify(emailSender).sendEmail(anyString(), anyString(), any(Locale.class), anyString());
    }

    @Test
    void shouldResetPasswordWithToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(generateValidJwtToken("test@test.com"));
        request.setNewPassword("new_password");

        String email = "test@test.com";
        AuthUser user = new AuthUser();

        AppUser appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName("Doe");
        appUser = appUserRepository.save(appUser);

        user.setAppUser(appUser);
        user.setPassword(passwordEncoder.encode("old_password"));
        user.setEmail(email);

        authUserRepository.save(user);

        when(jwtUtils.validateToken(request.getToken())).thenReturn(true);
        when(jwtUtils.isTokenTypeValid(request.getToken(), TokenType.RESET_PASSWORD)).thenReturn(true);
        when(jwtUtils.extractEmail(request.getToken())).thenReturn(email);

        passwordResetService.resetPasswordWithToken(request);

        AuthUser updatedUser = authUserRepository.findByEmail(user.getEmail()).orElseThrow();
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

        AppUser appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName("Doe");
        appUser.setPhone("123456789");
        appUserRepository.save(appUser);

        AuthUser user = new AuthUser();
        user.setEmail("test@test.com");
        user.setPassword("hashed_password");
        user.setAppUser(appUser);
        authUserRepository.save(user);

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
        AppUser appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName("Doe");
        appUser.setPhone("123456789");

        AuthUser user = new AuthUser();
        user.setEmail("test@test.com");
        user.setPassword("hashed_password");
        user.setAppUser(appUser);

        appUserRepository.save(appUser);
        authUserRepository.save(user);

        doThrow(new MessagingException("Error sending email"))
                .when(emailSender).sendEmail(anyString(), anyString(), any(Locale.class), anyString());

        ApiException exception = assertThrows(ApiException.class, () ->
                passwordResetService.requestPasswordReset(user.getEmail())
        );

        assertTrue(exception.getMessage().contains("Your account is not active. Please contact support."));
    }

    private String generateValidJwtToken(String email) {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        when(jwtUtils.getSecretKey()).thenReturn(secretKey);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour to expire

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("TokenType", TokenType.CONFIRMATION.toString())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}