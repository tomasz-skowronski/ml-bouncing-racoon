package de.magicline.racoon.api.dto;

import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;

@SuppressWarnings("unused")
public class ValidateEmailsResponse {

    private final String taskId;

    public ValidateEmailsResponse(RTEVAsyncResult result) {
        this.taskId = result.getInfo();
    }

    public String getTaskId() {
        return taskId;
    }
}
