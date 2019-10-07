package de.magicline.racoon.service.taskresult;

public final class ValidatedEmail {

    private final String email;
    private final ValidationStatus status;

    public ValidatedEmail(String email, ValidationStatus status) {
        this.email = email;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public boolean isInvalid() {
        return getStatusType() == ValidationStatus.Type.INVALID;
    }

    public boolean isSuspect() {
        return getStatusType() == ValidationStatus.Type.SUSPECT;
    }

    public boolean isIndeterminate() {
        return getStatusType() == ValidationStatus.Type.INDETERMINATE;
    }

    public boolean isValid() {
        return getStatusType() == ValidationStatus.Type.VALID;
    }

    public ValidationStatus.Type getStatusType() {
        return status.getType();
    }
}
