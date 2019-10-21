package de.magicline.racoon.service.task;

import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.service.rtev.EmailValidationService;
import de.magicline.racoon.service.status.StatusPublisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskListener {

    private static final Logger LOGGER = LogManager.getLogger(TaskListener.class);

    private final EmailValidationService emailValidationService;
    private final StatusPublisher statusPublisher;

    public TaskListener(EmailValidationService emailValidationService1, StatusPublisher statusPublisher) {
        this.emailValidationService = emailValidationService1;
        this.statusPublisher = statusPublisher;
    }

    @RabbitListener(queues = RabbitConfiguration.TASK_QUEUE)
    public void onTaskCompleted(String taskId) {
        LOGGER.debug("onTaskCompleted {} ", taskId);
        try {
            TaskResult taskResult = emailValidationService.downloadTaskResult(taskId);
            statusPublisher.publishStatusMessages(taskResult);
        } catch (Exception e) {
            LOGGER.error("Exception on text message order:" + taskId, e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
