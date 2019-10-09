package de.magicline.racoon.api;

import de.magicline.racoon.service.rtev.EmailValidationService;
import de.magicline.racoon.service.taskresult.TaskResult;
import de.magicline.racoon.service.taskresult.TasksCallbacksService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/racoon/tasks")
public class TasksController {

    private final TasksCallbacksService tasksCallbacksService;
    private final EmailValidationService emailValidationService;


    public TasksController(TasksCallbacksService tasksCallbacksService, EmailValidationService emailValidationService) {
        this.tasksCallbacksService = tasksCallbacksService;
        this.emailValidationService = emailValidationService;
    }

    @GetMapping("/callbacks")
    public ResponseEntity<String> complete(@RequestParam("taskid") String taskId) {
        tasksCallbacksService.complete(taskId);
        return ResponseEntity.accepted().body("OK");
    }

    @GetMapping("/{taskId}")
    public TaskResult getTaskResult(@PathVariable String taskId) {
        return emailValidationService.downloadTaskResult(taskId);
    }

}
