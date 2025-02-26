package pl.kamann.entities.appuser;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppUserTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "app_user_id", nullable = false, unique = true)
    private AppUser appUser;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private TokenType tokenType;

    private LocalDateTime expirationDate;

    public Boolean isTokenExpired() {
        return expirationDate == null || LocalDateTime.now().isAfter(expirationDate);
    }
}
