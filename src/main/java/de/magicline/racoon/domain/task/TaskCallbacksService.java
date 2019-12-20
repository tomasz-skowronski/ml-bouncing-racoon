package de.magicline.racoon.domain.task;

import de.magicline.racoon.config.RabbitConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskCallbacksService {

    private static final Logger LOGGER = LogManager.getLogger(TaskCallbacksService.class);

    private final RabbitTemplate rabbitTemplate;

    public TaskCallbacksService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void complete(String taskId) {
        LOGGER.info("complete task {}", taskId);
        rabbitTemplate.convertAndSend(RabbitConfiguration.TASK_QUEUE, taskId);
    }
}
