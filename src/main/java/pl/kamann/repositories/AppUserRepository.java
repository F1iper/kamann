package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Role;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRolesContaining(Role role);

    Page<AppUser> findByRolesContaining(Role role, Pageable pageable);

    @Query("SELECT DISTINCT u FROM AppUser u JOIN u.roles r WHERE r IN :roles")
    Page<AppUser> findUsersByRoles(Pageable pageable, @Param("roles") List<Role> roles);
}
