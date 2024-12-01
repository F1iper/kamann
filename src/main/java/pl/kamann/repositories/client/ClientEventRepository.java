package pl.kamann.repositories.client;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.entities.ClientEventHistory;

public interface ClientEventRepository extends JpaRepository<ClientEventHistory, Long> {
}
