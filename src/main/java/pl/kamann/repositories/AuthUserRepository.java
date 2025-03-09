package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AuthUser;
import pl.kamann.entities.appuser.Role;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    @Query("SELECT u FROM AuthUser u JOIN u.roles r WHERE r = :role")
    Page<AuthUser> findUsersByRoleWithRoles(Pageable pageable, @Param("role") Role role);

    Page<AuthUser> findByRolesContaining(Role role, Pageable pageable);
}