package de.magicline.racoon.service.taskresult;

import de.magicline.racoon.service.rtev.EmailValidationService;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class TasksCallbacksService {

    private static final Logger LOGGER = LogManager.getLogger(TasksCallbacksService.class);

    private final TaskResultProcessor taskResultProcessor;
    private final EmailValidationService emailValidationService;

    public TasksCallbacksService(TaskResultProcessor taskResultProcessor, EmailValidationService emailValidationService) {
        this.taskResultProcessor = taskResultProcessor;
        this.emailValidationService = emailValidationService;
    }

    public void complete(String taskId) {
        LOGGER.info("complete task {}", taskId);
        // TODO persist
        // TODO use rabbitmq
        new Thread(() -> handleTask(taskId))
                .start();
    }

    private void handleTask(String taskId) {
        Map<ValidationStatus.Type, List<ValidatedEmail>> result =
                taskResultProcessor.process(
                        emailValidationService.downloadTaskResult(taskId));
        LOGGER.info(result);
    }
}
