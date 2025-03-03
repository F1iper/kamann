package pl.kamann.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Token cannot be blank")
    private String token;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}