package de.magicline.racoon.service.taskresult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class TaskResultProcessor {

    public Map<ValidationStatus.Type, List<ValidatedEmail>> process(TaskResult report) {
        Map<ValidationStatus.Type, List<ValidatedEmail>> byStatusType = report.getRows()
                .stream()
                .map(this::toValidatedEmail)
                .collect(Collectors.groupingBy(ValidatedEmail::getStatusType));
        // TODO
        return byStatusType;
    }

    private ValidatedEmail toValidatedEmail(RowValue row) {
        return new ValidatedEmail(row.getEmail(), ValidationStatus.of(row.getResult()));
    }

}
