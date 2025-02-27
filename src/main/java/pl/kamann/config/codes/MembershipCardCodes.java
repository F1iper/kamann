package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MembershipCardCodes {
    CARD_NOT_FOUND("CARD_NOT_FOUND"),
    CARD_NOT_ACTIVE("CARD_NOT_ACTIVE"),
    NO_ENTRANCES_LEFT("NO_ENTRANCES_LEFT"),
    INVALID_CARD_STATE("INVALID_CARD_STATE"),
    CARD_ALREADY_EXISTS("CARD_ALREADY_EXISTS"),
    MULTIPLE_ACTIVE_CARDS("MULTIPLE_ACTIVE_CARDS"),
    UNKNOWN_CARD_TYPE("UNKNOWN_CARD_TYPE");

    private final String code;
}
