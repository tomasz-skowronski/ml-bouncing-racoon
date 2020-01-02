package de.magicline.racoon.api.dto;

import de.magicline.racoon.domain.provider.dto.ValidationMode;
import javax.validation.constraints.NotNull;

import java.beans.ConstructorProperties;
import java.util.List;

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
}
