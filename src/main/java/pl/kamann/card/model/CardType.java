package pl.kamann.card.model;

public enum CardType {
    SINGLE_ENTRY("Single Entry"),
    MONTHLY_4("Monthly - 4 Entrances"),
    MONTHLY_8("Monthly - 8 Entrances"),
    MONTHLY_12("Monthly - 12 Entrances");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
