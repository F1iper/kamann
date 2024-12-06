package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Role;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Page<AppUser> findByRolesContaining(Role role, Pageable pageable);
}
