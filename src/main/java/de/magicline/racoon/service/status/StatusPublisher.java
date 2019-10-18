package de.magicline.racoon.service.status;

import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.service.rtev.RTEVValidationStatus;
import de.magicline.racoon.service.task.TaskResult;
import de.magicline.racoon.service.task.ValidatedEmail;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StatusPublisher {

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
        Map<ValidationStatus, List<ValidatedEmail>> byStatusType = dispatcher.dispatch(taskResult);
        byStatusType.forEach((status, emails) ->
                partition(emails).forEach(batch ->
                        sendToMQ(taskResult.getTaskId(), status, batch)));
    }

    private List<List<StatusItem>> partition(List<ValidatedEmail> emails) {
        List<StatusItem> items = emails.stream()
                .map(e -> new StatusItem(e.getEmail()))
                .collect(Collectors.toList());
        return ListUtils.partition(items, batchSize);
    }

    private void sendToMQ(String taskId, ValidationStatus status, List<StatusItem> statusItems) {
        RTEVValidationStatus.Type type = RTEVValidationStatus.Type.valueOf(status.getType());
        rabbitTemplate.convertAndSend(
                RabbitConfiguration.toStatusQueueName(type),
                new StatusMessage(taskId, new ValidationStatusDto(status), statusItems));
    }

}
