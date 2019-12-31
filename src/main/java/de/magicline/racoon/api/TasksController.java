package de.magicline.racoon.api;

import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.status.dto.ValidationStatus;
import de.magicline.racoon.domain.status.dto.ValidationStatusDto;
import de.magicline.racoon.domain.task.TaskCallbacksService;
import de.magicline.racoon.domain.task.TaskResultFetcher;
import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.dto.TaskResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.annotations.VisibleForTesting;

@RestController
@RequestMapping("/racoon/tasks")
public class TasksController {

    private final TaskCallbacksService tasksCallbacksService;
    private final TaskResultFetcher taskResultFetcher;

    public TasksController(TaskCallbacksService tasksCallbacksService, TaskResultFetcher taskResultFetcher) {
        this.tasksCallbacksService = tasksCallbacksService;
        this.taskResultFetcher = taskResultFetcher;
    }

    @VisibleForTesting
    @GetMapping("/callbacks")
    public ResponseEntity<String> complete(@RequestParam("taskid") String taskId) {
        tasksCallbacksService.complete(taskId);
        return ResponseEntity.accepted().body("OK");
    }

    @VisibleForTesting
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable String taskId) {
        return taskResultFetcher.findByTaskId(taskId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @VisibleForTesting
    @GetMapping("/{taskId}/result")
    public TaskResult getTaskResult(@PathVariable String taskId) {
        return taskResultFetcher.fetchByTaskId(taskId);
    }

    @GetMapping("/statuses")
    public List<ValidationStatusDto> getStatuses() {
        return Arrays.stream(RTEVValidationStatus.values())
                .map(ValidationStatusDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/statuses/{code}")
    public ValidationStatus getStatuses(@PathVariable int code) {
        return new ValidationStatusDto(RTEVValidationStatus.of(code));
    }

}
