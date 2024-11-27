package pl.kamann.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.ClientEventHistory;

public interface ClientEventRepository extends JpaRepository<ClientEventHistory, Long> {
}
