package pl.kamann.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.user.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
