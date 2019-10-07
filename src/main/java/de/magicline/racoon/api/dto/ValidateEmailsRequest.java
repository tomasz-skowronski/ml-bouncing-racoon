package de.magicline.racoon.api.dto;

import java.beans.ConstructorProperties;
import java.util.List;

public class ValidateEmailsRequest {

    private final List<String> emails;

    @ConstructorProperties("emails")
    public ValidateEmailsRequest(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getEmails() {
        return emails;
    }

}
