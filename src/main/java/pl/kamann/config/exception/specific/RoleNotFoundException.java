package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;

public class RoleNotFoundException extends ApiException {
    public RoleNotFoundException() {
        super("Role not found.", HttpStatus.NOT_FOUND, AuthCodes.ROLE_NOT_FOUND.name());
    }
}