package de.magicline.racoon.domain.task;

import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.provider.EmailValidator;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.dto.TaskResult;
import de.magicline.racoon.domain.task.persistance.TaskRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskResultFetcher {

    private final TaskRepository taskRepository;
    private final EmailValidator emailValidator;
    private final Clock clock;

    public TaskResultFetcher(TaskRepository taskRepository,
                             EmailValidator emailValidator,
                             Clock clock) {
        this.taskRepository = taskRepository;
        this.emailValidator = emailValidator;
        this.clock = clock;
    }

    @Transactional
    public TaskResult fetchByTaskId(String taskId) {
        ValidationResult validationResult = emailValidator.downloadValidationResult(taskId);
        Task task = updateModifiedDate(taskId);
        updateMetrics(task);
        return new TaskResult(
                taskId,
                task.getTenant(),
                validationResult);
    }

    private Task updateModifiedDate(String taskId) {
        Optional<Task> task = taskRepository.findByTaskId(taskId)
                .map(t -> t.withModifiedDate(clock.instant()));
        task.ifPresent(taskRepository::update);
        return task.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, taskId));
    }

    private void updateMetrics(Task task) {
        RacoonMetrics.durationOfTask(
                Duration.between(task.getCreatedDate(), task.getModifiedDate()));
    }
}
