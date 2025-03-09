package pl.kamann.services;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.security.jwt.JwtUtils;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.AuthUserStatus;
import pl.kamann.entities.appuser.TokenType;
import pl.kamann.repositories.AppUserRepository;
import pl.kamann.repositories.AuthUserRepository;
import pl.kamann.services.email.EmailSender;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmUserService {

    private final EmailSender emailSender;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final AuthUserRepository authUserRepository;
    private final AppUserRepository appUserRepository;

    // Track scheduled deletion tasks by email
    private final Map<String, ScheduledFuture<?>> deletionTasks = new ConcurrentHashMap<>();

    public void sendConfirmationEmail(AuthUser authUser) {
        String token = tokenService.generateToken(authUser.getEmail(), TokenType.CONFIRMATION, 15 * 60 * 1000);

        try {
            String confirmationLink = tokenService.generateConfirmationLink(token, tokenService.getConfirmationLink());
            emailSender.sendEmail(authUser.getEmail(), confirmationLink, Locale.ENGLISH, "registration");
            log.info("Confirmation email sent successfully to user: {}", authUser.getEmail());

            scheduleUserDeletion(authUser.getEmail());

        } catch (MessagingException e) {
            log.error("Error sending the confirmation email to user: {}", authUser.getEmail(), e);
            throw new ApiException(
                    "Error sending the confirmation email.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AuthCodes.CONFIRMATION_EMAIL_ERROR.name()
            );
        }
    }

    private void scheduleUserDeletion(String email) {
        cancelDeletionTask(email);

        ScheduledFuture<?> task = scheduledExecutorService.schedule(() -> {
            Optional<AuthUser> authUserOptional = authUserRepository.findByEmail(email);
            if (authUserOptional.isPresent() && !authUserOptional.get().isEnabled()) {
                authUserRepository.delete(authUserOptional.get());
                log.info("User {} deleted due to inactivity after {} minutes", email, 15);
                deletionTasks.remove(email);
            }
        }, 15, TimeUnit.MINUTES);

        deletionTasks.put(email, task);
    }

    private void cancelDeletionTask(String email) {
        ScheduledFuture<?> task = deletionTasks.get(email);
        if (task != null && !task.isDone() && !task.isCancelled()) {
            task.cancel(false);
            log.info("Cancelled scheduled deletion task for user: {}", email);
        }
        deletionTasks.remove(email);
    }

    @Transactional
    public void confirmUserAccount(String token) {
        log.info("Confirming user account for token: {}", token);

        if (!jwtUtils.validateToken(token)) {
            throw new ApiException(
                    "Invalid or expired confirmation Token",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.INVALID_TOKEN.name()
            );
        }

        if (!jwtUtils.isTokenTypeValid(token, TokenType.CONFIRMATION)) {
            throw new ApiException(
                    "Token type is invalid",
                    HttpStatus.UNAUTHORIZED,
                    AuthCodes.INVALID_TOKEN.name()
            );
        }

        String email = jwtUtils.extractEmail(token);

        AuthUser user = authUserRepository.findByEmail(email).orElseThrow(() ->
                new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND,
                        AuthCodes.USER_NOT_FOUND.name()
                )
        );

        if (user.isEnabled()) {
            throw new ApiException(
                    "User is already confirmed",
                    HttpStatus.BAD_REQUEST,
                    AuthCodes.USER_ALREADY_CONFIRMED.name()
            );
        }

        user.setEnabled(true);
        user.setStatus(AuthUserStatus.ACTIVE);
        authUserRepository.save(user);

        if (!appUserRepository.existsByAuthUser(user)) {
            AppUser newAppUser = new AppUser();
            newAppUser.setAuthUser(user);
            appUserRepository.save(newAppUser);
        }

        cancelDeletionTask(email);

        log.info("User account confirmed for: {}", user.getEmail());
    }
}