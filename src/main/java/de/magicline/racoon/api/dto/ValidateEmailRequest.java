package de.magicline.racoon.api.dto;

import javax.validation.constraints.NotNull;

import java.beans.ConstructorProperties;

public class ValidateEmailRequest {

    @NotNull
    private final String email;

    @ConstructorProperties("email")
    public ValidateEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
