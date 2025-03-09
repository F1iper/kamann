package pl.kamann.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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

    @Autowired
    private JwtUtils jwtUtils;

    private AppUser testUser;
    private AuthUser testAuthUser;

    @BeforeEach
    void setup() {
        testAuthUser = AuthUser.builder()
                .email("test@test.com")
                .password("hashed_password")
                .enabled(false)
                .build();

        testUser = AppUser.builder()
                .firstName("Test")
                .lastName("User")
                .authUser(testAuthUser)
                .build();

        testAuthUser.setAppUser(testUser);
        appUserRepository.save(testUser);
    }

    @Test
    void shouldConfirmAccount() {
        String token = generateValidJwtToken(testAuthUser.getEmail());
        confirmUserService.confirmUserAccount(token);

        AppUser updatedUser = appUserRepository.findByEmail(testAuthUser.getEmail()).orElseThrow();
        assertTrue(updatedUser.getAuthUser().isEnabled());
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String invalidJwtToken = generateInvalidJwtToken();

        Exception exception = assertThrows(RuntimeException.class, () ->
                confirmUserService.confirmUserAccount(invalidJwtToken)
        );
        assertTrue(exception.getMessage().contains("Invalid or expired confirmation Token"));
    }

    @Test
    void shouldNotConfirmAlreadyConfirmedAccount() {
        testAuthUser.setEnabled(true);
        appUserRepository.save(testUser);

        String validJwtToken = generateValidJwtToken(testAuthUser.getEmail());

        ApiException exception = assertThrows(ApiException.class, () ->
                confirmUserService.confirmUserAccount(validJwtToken)
        );

        assertTrue(exception.getMessage().contains("User is already confirmed"));

        AppUser updatedUser = appUserRepository.findByEmail(testAuthUser.getEmail()).orElseThrow();
        assertTrue(updatedUser.getAuthUser().isEnabled());
    }

    @Test
    void shouldSendConfirmationEmailAfterActivation() throws MessagingException {
        confirmUserService.sendConfirmationEmail(testAuthUser);

        verify(emailSender).sendEmail(eq(testAuthUser.getEmail()), anyString(), any(), eq("registration"));
    }

    private String generateInvalidJwtToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 10000); // Expired token

        return Jwts.builder()
                .setSubject("testuser@example.com")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("TokenType", TokenType.CONFIRMATION.toString())
                .signWith(jwtUtils.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateValidJwtToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour to expire

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("TokenType", TokenType.CONFIRMATION.toString())
                .signWith(jwtUtils.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}