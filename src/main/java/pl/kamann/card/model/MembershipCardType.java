package pl.kamann.card.model;

import lombok.Getter;

@Getter
public enum MembershipCardType {
    SINGLE_ENTRY("Single Entry", 1, null),
    MONTHLY_4("Monthly - 4 Entrances", 4, 30),
    MONTHLY_8("Monthly - 8 Entrances", 8, 30),
    MONTHLY_12("Monthly - 12 Entrances", 12, 30);

    private final String displayName;
    private final int maxEntrances;
    private final Integer validDays;

    MembershipCardType(String displayName, int maxEntrances, Integer validDays) {
        this.displayName = displayName;
        this.maxEntrances = maxEntrances;
        this.validDays = validDays;
    }

    @Override
    public String toString() {
        return displayName;
    }
}