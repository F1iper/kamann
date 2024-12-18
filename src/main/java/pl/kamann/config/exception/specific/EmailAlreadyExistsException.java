package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.AuthCodes;
import pl.kamann.config.exception.handler.ApiException;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' is already registered.", email), HttpStatus.CONFLICT, AuthCodes.EMAIL_ALREADY_EXISTS.name());
    }
}