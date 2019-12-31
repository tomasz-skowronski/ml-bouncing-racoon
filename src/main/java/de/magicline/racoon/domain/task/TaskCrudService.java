package de.magicline.racoon.domain.task;

import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.persistance.TaskRepository;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCrudService {

    private final TaskRepository taskRepository;
    private final Clock clock;

    public TaskCrudService(TaskRepository taskRepository,
                           Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<Task> read(String taskId) {
        return taskRepository.findByTaskId(taskId);
    }

    @Transactional
    public void create(String taskId, String tenant) {
        taskRepository.insert(new Task(taskId, tenant, clock.instant()));
    }

}
