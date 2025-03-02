package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamann.entities.appuser.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT t FROM Token t WHERE t.token = :token AND t.tokenType = 'CONFIRMATION'")
    Optional<Token> findConfirmationToken(@Param("token") String token);

    @Query("SELECT t FROM Token t WHERE t.token = :token")
    Optional<Token> findByToken(@Param("token") String token);
}
