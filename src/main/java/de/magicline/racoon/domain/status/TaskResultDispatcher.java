package de.magicline.racoon.domain.status;

import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.status.dto.ValidationStatus;
import de.magicline.racoon.domain.task.dto.TaskResult;
import de.magicline.racoon.domain.task.dto.ValidatedEmail;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TaskResultDispatcher {

    private static final Logger LOGGER = LogManager.getLogger(TaskResultDispatcher.class);

    @SuppressWarnings("squid:S3864")
    Map<ValidationStatus, List<ValidatedEmail>> dispatch(TaskResult taskResult) {
        Map<ValidationStatus, List<ValidatedEmail>> byStatus = taskResult.getResult()
                .getRows()
                .stream()
                .peek(r -> RacoonMetrics.incrementValidationStatus(r.getResult()))
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
