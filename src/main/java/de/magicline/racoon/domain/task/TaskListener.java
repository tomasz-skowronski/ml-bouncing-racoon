package de.magicline.racoon.domain.task;

import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.domain.status.StatusPublisher;
import de.magicline.racoon.domain.task.dto.TaskResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskListener {

    private static final Logger LOGGER = LogManager.getLogger(TaskListener.class);

    private final TasksDownloader tasksDownloader;
    private final StatusPublisher statusPublisher;

    public TaskListener(TasksDownloader tasksDownloader, StatusPublisher statusPublisher) {
        this.tasksDownloader = tasksDownloader;
        this.statusPublisher = statusPublisher;
    }

    @RabbitListener(queues = RabbitConfiguration.TASK_QUEUE)
    public void onTaskCompleted(String taskId) {
        LOGGER.info("onTaskCompleted: {} ", taskId);
        try {
            TaskResult taskResult = tasksDownloader.downloadTaskResult(taskId);
            statusPublisher.publishStatusMessages(taskResult);
        } catch (Exception e) {
            LOGGER.error("onTaskCompleted FAILED: {}", taskId, e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
