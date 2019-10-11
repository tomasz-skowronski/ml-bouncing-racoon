package de.magicline.racoon.api.dto;

import javax.validation.constraints.NotNull;

import java.beans.ConstructorProperties;
import java.util.List;

public class ValidateEmailsRequest {

    @NotNull
    private final List<String> emails;

    @ConstructorProperties("emails")
    public ValidateEmailsRequest(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getEmails() {
        return emails;
    }

}
