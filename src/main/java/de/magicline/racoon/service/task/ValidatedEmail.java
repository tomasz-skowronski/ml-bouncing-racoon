package de.magicline.racoon.service.task;

import de.magicline.racoon.service.rtev.RTEVValidationStatus;

public final class ValidatedEmail {

    private final String email;
    private final RTEVValidationStatus status;

    public ValidatedEmail(String email, int status) {
        this.email = email;
        this.status = RTEVValidationStatus.of(status);
    }

    public String getEmail() {
        return email;
    }

    public RTEVValidationStatus getStatus() {
        return status;
    }

}
