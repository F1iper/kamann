package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.appuser.AppUserTokens;

import java.util.Optional;

public interface AppUserTokensRepository extends JpaRepository<AppUserTokens, Long> {
    Optional<AppUserTokens> findByResetPasswordToken(String token);
}
