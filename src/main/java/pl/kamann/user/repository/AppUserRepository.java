package pl.kamann.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.auth.role.model.Role;
import pl.kamann.user.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRolesContaining(Role role);

}
