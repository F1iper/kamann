package pl.kamann.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.Role;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @Query("SELECT u FROM AppUser u JOIN u.roles r WHERE r.name = :roleName")
    List<AppUser> findByRoleName(@Param("roleName") String roleName);

    Optional<AppUser> findByEmail(String email);

    Page<AppUser> findByRolesContaining(Role role, Pageable pageable);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM AppUser u")
    Page<AppUser> findAllWithRoles(Pageable pageable);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM AppUser u JOIN FETCH u.roles r WHERE u.email = :email")
    Optional<AppUser> findAppUserWithRolesByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT DISTINCT u FROM AppUser u JOIN u.roles r WHERE r = :role")
    Page<AppUser> findUsersByRoleWithRoles(Pageable pageable, Role role);
}
