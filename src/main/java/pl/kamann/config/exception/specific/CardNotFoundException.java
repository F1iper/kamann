package pl.kamann.config.exception.specific;

import org.springframework.http.HttpStatus;
import pl.kamann.config.codes.MembershipCardCodes;
import pl.kamann.config.exception.handler.ApiException;

public class CardNotFoundException extends ApiException {
    public CardNotFoundException() {
        super("Membership card not found.", HttpStatus.NOT_FOUND, MembershipCardCodes.CARD_NOT_FOUND.name());
    }
}