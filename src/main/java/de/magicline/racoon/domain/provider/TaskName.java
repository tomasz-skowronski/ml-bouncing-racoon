package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class TaskName {

    private final String tenant;
    private final int size;

    TaskName(ValidateEmailsRequest request) {
        this.tenant = request.getTenant();
        this.size = request.getEmails().size();
    }

    String generateName() {
        return String.join(":", tenant, String.valueOf(size), getNow());
    }

    private String getNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
