package pl.kamann.config.exception.specific;

import pl.kamann.config.exception.handler.ApiException;

import org.springframework.http.HttpStatus;
import pl.kamann.config.global.Codes;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' is already registered.", email), HttpStatus.CONFLICT, Codes.EMAIL_ALREADY_EXISTS);
    }
}