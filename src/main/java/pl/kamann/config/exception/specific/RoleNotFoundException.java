package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class RoleNotFoundException extends ApiException {
    public RoleNotFoundException() {
        super("Role not found.", HttpStatus.NOT_FOUND, Codes.ROLE_NOT_FOUND);
    }
}