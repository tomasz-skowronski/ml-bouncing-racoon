package de.magicline.racoon.service.task;

public final class ValidatedEmail {

    private final String email;
    private final ValidationStatus status;

    public ValidatedEmail(String email, int status) {
        this.email = email;
        this.status = ValidationStatus.of(status);
    }

    public String getEmail() {
        return email;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public ValidationStatus.Type getStatusType() {
        return status.getType();
    }
}
