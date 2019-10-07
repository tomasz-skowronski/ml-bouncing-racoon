package de.magicline.racoon.api.dto;

import de.magicline.racoon.service.rtev.RTEVResult;

@SuppressWarnings("unused")
public class ValidateEmailResponse {

    private final int status;
    private final String info;
    private final String details;

    public ValidateEmailResponse(RTEVResult result) {
        this(result.getStatus(), result.getInfo(), result.getDetails());
    }

    private ValidateEmailResponse(int status, String info, String details) {
        this.status = status;
        this.info = info;
        this.details = details;
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    public String getDetails() {
        return details;
    }
}
