package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.appuser.AppUserStatus;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
    private AppUserStatus status;
    private LocalDate cardExpiryDate;
}