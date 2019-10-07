package de.magicline.racoon.api;

import de.magicline.racoon.service.taskresult.TasksCallbacksService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/racoon/tasks-callbacks")
public class TasksCallbacksController {

    private final TasksCallbacksService tasksCallbacksService;

    public TasksCallbacksController(TasksCallbacksService tasksCallbacksService) {
        this.tasksCallbacksService = tasksCallbacksService;
    }

    @GetMapping
    public void validateEmailsAsync(@RequestParam("taskid") String taskId) {
        tasksCallbacksService.complete(taskId);
    }

}
