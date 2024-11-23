package pl.kamann.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamann.history.model.ClientEventHistory;

public interface ClientEventRepository extends JpaRepository<ClientEventHistory, Long> {
}
