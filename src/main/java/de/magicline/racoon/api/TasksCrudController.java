package de.magicline.racoon.api;

import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.status.dto.ValidationStatus;
import de.magicline.racoon.domain.status.dto.ValidationStatusDto;
import de.magicline.racoon.domain.task.TaskCrudService;
import de.magicline.racoon.domain.task.dto.Task;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
@RestController
@RequestMapping("/racoon/tasks")
public class TasksCrudController {

    private final TaskCrudService taskCrudService;

    public TasksCrudController(TaskCrudService taskCrudService) {
        this.taskCrudService = taskCrudService;
    }

    @PostMapping("/{taskId}")
    public void storeTask(@PathVariable String taskId, @RequestParam String tenant) {
        taskCrudService.create(taskId, tenant);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable String taskId) {
        return taskCrudService.read(taskId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
