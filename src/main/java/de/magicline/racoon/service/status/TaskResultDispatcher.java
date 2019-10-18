package de.magicline.racoon.service.status;

import de.magicline.racoon.service.task.TaskResult;
import de.magicline.racoon.service.task.ValidatedEmail;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TaskResultDispatcher {

    private static final Logger LOGGER = LogManager.getLogger(TaskResultDispatcher.class);

    Map<ValidationStatus, List<ValidatedEmail>> dispatch(TaskResult taskResult) {
        Map<ValidationStatus, List<ValidatedEmail>> byStatus = taskResult.getRows()
                .stream()
                .map(r -> new ValidatedEmail(r.getEmail(), r.getResult()))
                .collect(Collectors.groupingBy(ValidatedEmail::getStatus));
        LOGGER.info("dispatch messages: {}", sum(byStatus));
        return byStatus;
    }

    private Map<ValidationStatus, Integer> sum(Map<ValidationStatus, List<ValidatedEmail>> result) {
        return result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()));
    }

}
