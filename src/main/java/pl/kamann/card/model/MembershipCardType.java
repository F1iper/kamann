package pl.kamann.card.model;

import lombok.Getter;

@Getter
public enum MembershipCardType {
    SINGLE_ENTRY("Single Entry"),
    MONTHLY_4("Monthly - 4 Entrances"),
    MONTHLY_8("Monthly - 8 Entrances"),
    MONTHLY_12("Monthly - 12 Entrances");

    private final String displayName;

    MembershipCardType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
