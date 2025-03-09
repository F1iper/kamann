package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.appuser.AuthUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByAuthUser(AuthUser user);

    Optional<AppUser> findByAuthUser(AuthUser authUser);

    @Query("""
            SELECT au.appUser FROM AuthUser au 
            WHERE au.email = :email
        """)
    Optional<AppUser> findByEmail(@Param("email") String email);

}
