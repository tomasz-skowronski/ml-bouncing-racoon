package de.magicline.racoon.api.dto;

import javax.validation.constraints.NotNull;

import java.beans.ConstructorProperties;
import java.util.List;

public class ValidateEmailsRequest {

    @NotNull
    private final List<String> emails;
    @NotNull
    private final String tenant;

    @ConstructorProperties({"emails", "tenant"})
    public ValidateEmailsRequest(List<String> emails, String tenant) {
        this.emails = emails;
        this.tenant = tenant;
    }

    public List<String> getEmails() {
        return emails;
    }

    public String getTenant() {
        return tenant;
    }
}
