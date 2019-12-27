package de.magicline.racoon.domain.task;

import de.magicline.racoon.domain.provider.EmailValidationService;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.task.dto.TaskResult;

import org.springframework.stereotype.Service;

@Service
public class TasksDownloader {

    private final EmailValidationService emailValidationService;

    public TasksDownloader(EmailValidationService emailValidationService) {
        this.emailValidationService = emailValidationService;
    }

    public TaskResult downloadTaskResult(String taskId) {
        ValidationResult validationResult = emailValidationService.downloadValidationResult(taskId);
        String tenant = "tenant1"; // TODO FIXME
        return new TaskResult(
                taskId,
                tenant,
                validationResult);
    }
}
