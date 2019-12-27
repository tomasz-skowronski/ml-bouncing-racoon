package de.magicline.racoon.domain.status;

import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.status.dto.StatusItem;
import de.magicline.racoon.domain.status.dto.StatusMessage;
import de.magicline.racoon.domain.status.dto.ValidationStatus;
import de.magicline.racoon.domain.status.dto.ValidationStatusDto;
import de.magicline.racoon.domain.task.dto.TaskResult;
import de.magicline.racoon.domain.task.dto.ValidatedEmail;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StatusPublisher {

    private static final Logger LOGGER = LogManager.getLogger(StatusPublisher.class);

    private final TaskResultDispatcher dispatcher;
    private final RabbitTemplate rabbitTemplate;
    private final int batchSize;

    public StatusPublisher(TaskResultDispatcher dispatcher,
                           RabbitTemplate rabbitTemplate,
                           @Value("${app.task.status.batch}") int batchSize) {
        this.dispatcher = dispatcher;
        this.rabbitTemplate = rabbitTemplate;
        this.batchSize = batchSize;
    }

    public void publishStatusMessages(TaskResult taskResult) {
        dispatcher.dispatch(taskResult).forEach((status, emails) ->
                partition(emails).forEach(batch -> {
                    StatusMessage statusMessage = new StatusMessage(
                            taskResult.getTaskId(),
                            taskResult.getTenant(),
                            new ValidationStatusDto(status),
                            batch);
                    sendToMQ(statusMessage, status);
                }));
    }

    private List<List<StatusItem>> partition(List<ValidatedEmail> emails) {
        List<StatusItem> items = emails.stream()
                .map(e -> new StatusItem(e.getEmail()))
                .collect(Collectors.toList());
        return ListUtils.partition(items, batchSize);
    }

    private void sendToMQ(StatusMessage statusMessage, ValidationStatus status) {
        RTEVValidationStatus.Type type = RTEVValidationStatus.Type.valueOf(status.getType());
        LOGGER.info("publish {}", statusMessage);
        rabbitTemplate.convertAndSend(
                RabbitConfiguration.toStatusQueueName(type),
                statusMessage);
    }

}
