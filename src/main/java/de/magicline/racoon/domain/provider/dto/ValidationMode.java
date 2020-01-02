package de.magicline.racoon.domain.provider.dto;

@SuppressWarnings("unused")
public enum ValidationMode {

    // Retries unavailable servers for 2 hours.
    EXPRESS,
    // Retries unavailable servers for 72 hours.
    EXTENSIVE;

    public String getValue() {
        return name().toLowerCase();
    }
}
