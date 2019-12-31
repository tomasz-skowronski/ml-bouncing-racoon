package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class TaskName {

    private final String tenant;
    private final int size;

    TaskName(ValidateEmailsRequest request) {
        this.tenant = request.getTenant();
        this.size = request.getEmails().size();
    }

    String generate(Clock clock) {
        return String.join("_",
                tenant,
                String.valueOf(size),
                format(LocalDateTime.now(clock)));
    }

    private String format(LocalDateTime dateTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
    }

}
