package de.magicline.racoon.api.dto;

import de.magicline.racoon.domain.provider.dto.ValidationMode;
import javax.validation.constraints.NotNull;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class ValidateEmailsRequest {

    @NotNull
    private final List<String> emails;
    @NotNull
    private final String tenant;
    private final ValidationMode validationMode;

    @ConstructorProperties({"emails", "tenant", "validationMode"})
    public ValidateEmailsRequest(List<String> emails, String tenant, ValidationMode validationMode) {
        this.emails = emails;
        this.tenant = tenant;
        this.validationMode = validationMode;
    }

    public List<String> getEmails() {
        return emails;
    }

    public String getTenant() {
        return tenant;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("emails", emails != null ? emails.size() : null)
                .add("tenant", tenant)
                .add("validationMode", validationMode)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidateEmailsRequest that = (ValidateEmailsRequest) o;
        return emails.equals(that.emails) &&
                tenant.equals(that.tenant) &&
                validationMode == that.validationMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emails, tenant, validationMode);
    }
}
