package de.magicline.racoon.api;

import de.magicline.racoon.domain.task.TaskCallbacksService;
import de.magicline.racoon.domain.task.TaskResultFetcher;
import de.magicline.racoon.domain.task.dto.TaskResult;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
@RestController
@RequestMapping("/racoon/tasks")
public class TasksFlowController {

    private final TaskCallbacksService tasksCallbacksService;
    private final TaskResultFetcher taskResultFetcher;

    public TasksFlowController(TaskCallbacksService tasksCallbacksService,
                               TaskResultFetcher taskResultFetcher) {
        this.tasksCallbacksService = tasksCallbacksService;
        this.taskResultFetcher = taskResultFetcher;
    }

    @GetMapping("/callbacks")
    public ResponseEntity<String> complete(@RequestParam("taskid") String taskId) {
        tasksCallbacksService.complete(taskId);
        return ResponseEntity.accepted().body("OK");
    }

    @GetMapping("/{taskId}/result")
    public TaskResult getTaskResult(@PathVariable String taskId) {
        return taskResultFetcher.fetchByTaskId(taskId);
    }

}
