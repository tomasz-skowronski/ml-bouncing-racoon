package de.magicline.racoon.service.status;

import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.service.task.TaskResult;
import de.magicline.racoon.service.task.ValidatedEmail;
import de.magicline.racoon.service.task.ValidationStatus;

import java.util.List;
import java.util.Map;
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

    private final RabbitTemplate rabbitTemplate;
    private int batchSize;

    public StatusPublisher(RabbitTemplate rabbitTemplate,
                           @Value("${app.task.status.batch}") int batchSize) {
        this.rabbitTemplate = rabbitTemplate;
        this.batchSize = batchSize;
    }

    public void publishStatusMessages(TaskResult taskResult) {
        Map<ValidationStatus.Type, List<ValidatedEmail>> byStatusType = taskResult.getRows()
                .stream()
                .map(r -> new ValidatedEmail(r.getEmail(), r.getResult()))
                .collect(Collectors.groupingBy(ValidatedEmail::getStatusType));
        LOGGER.info("publish status messages: {}", sum(byStatusType));
        byStatusType.forEach((type, emails) ->
                partition(emails).forEach(batch ->
                        sendToMQ(taskResult.getTaskId(), type, batch)));
    }

    private Map<ValidationStatus.Type, Integer> sum(Map<ValidationStatus.Type, List<ValidatedEmail>> result) {
        return result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()));
    }

    private List<List<StatusItem>> partition(List<ValidatedEmail> emails) {
        List<StatusItem> items = emails.stream()
                .map(e -> new StatusItem(e.getEmail(), e.getStatus().getCode()))
                .collect(Collectors.toList());
        return ListUtils.partition(items, batchSize);
    }

    private void sendToMQ(String taskId, ValidationStatus.Type type, List<StatusItem> statusItems) {
        rabbitTemplate.convertAndSend(
                RabbitConfiguration.toStatusQueueName(type),
                new StatusMessage(taskId, type.name().toLowerCase(), statusItems));
    }

}
