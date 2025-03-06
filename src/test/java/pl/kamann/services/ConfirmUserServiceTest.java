package pl.kamann.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.services.email.EmailSender;
import pl.kamann.testcontainers.config.TestContainersConfig;

import javax.crypto.SecretKey;
import java.util.Date;

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

    @Autowired
    private JwtUtils jwtUtils;

    private AppUser testUser;

    @BeforeEach
    void setup() {
        testUser = new AppUser();
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("hashed_password");
        testUser.setEnabled(false);
        appUserRepository.save(testUser);
    }

    @Test
    void shouldConfirmAccount() {
        appUserRepository.save(testUser);

        confirmUserService.confirmUserAccount(generateValidJwtToken(testUser.getEmail()));

        AppUser updatedUser = appUserRepository.findByEmail(testUser.getEmail()).orElseThrow();

        assertTrue(updatedUser.isEnabled());
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
        testUser.setEnabled(true);
        appUserRepository.save(testUser);

        String validJwtToken = generateValidJwtToken(testUser.getEmail());

        ApiException exception = assertThrows(ApiException.class, () ->
                confirmUserService.confirmUserAccount(validJwtToken)
        );

        assertTrue(exception.getMessage().contains("User is already confirmed"));

        AppUser updatedUser = appUserRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertTrue(updatedUser.isEnabled(), "User should remain enabled.");
    }

    @Test
    void shouldSendConfirmationEmailAfterActivation() throws MessagingException {
        appUserRepository.save(testUser);

        confirmUserService.sendConfirmationEmail(testUser);

        Mockito.verify(emailSender, Mockito.times(1))
                .sendEmail(Mockito.eq(testUser.getEmail()), Mockito.anyString(), Mockito.any(), Mockito.eq("registration"));
    }

    private String generateInvalidJwtToken() {
        SecretKey fakeKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 10000);

        return Jwts.builder()
                .setSubject("testuser@example.com")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(fakeKey, SignatureAlgorithm.HS256)
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