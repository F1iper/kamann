package pl.kamann.entities.appuser;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

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

    @OneToOne
    @JoinColumn(name = "app_user_id", nullable = false, unique = true)
    private AppUser appUser;

    private String confirmationToken;

    private String resetPasswordToken;

    private Date expirationDate;

    public Boolean isTokenExpired() {
        return new Date().after(expirationDate);
    }
}
