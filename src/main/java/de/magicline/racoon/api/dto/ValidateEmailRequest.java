package de.magicline.racoon.api.dto;

import java.beans.ConstructorProperties;

public class ValidateEmailRequest {

    private final String email;

    @ConstructorProperties("email")
    public ValidateEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
