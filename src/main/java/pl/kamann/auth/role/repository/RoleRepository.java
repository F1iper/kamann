package pl.kamann.auth.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.auth.role.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String user);
}
