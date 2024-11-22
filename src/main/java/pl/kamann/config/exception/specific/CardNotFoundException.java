package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;

public class CardNotFoundException extends ApiException {
    public CardNotFoundException() {
        super("Membership card not found.", HttpStatus.NOT_FOUND, Codes.CARD_NOT_FOUND);
    }
}