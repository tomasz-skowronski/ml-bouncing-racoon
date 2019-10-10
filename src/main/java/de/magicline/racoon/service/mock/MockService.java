package de.magicline.racoon.service.mock;

import de.magicline.racoon.service.rtev.RTEVRowValue;
import de.magicline.racoon.service.status.StatusPublisher;
import de.magicline.racoon.service.task.RowValue;
import de.magicline.racoon.service.task.TaskResult;
import de.magicline.racoon.service.task.ValidationStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class MockService {

    private final StatusPublisher statusPublisher;

    public MockService(StatusPublisher statusPublisher) {
        this.statusPublisher = statusPublisher;
    }

    public String validate(List<String> emails, BigDecimal correct) {
        AtomicInteger correctness = createCorrectnessCounter(emails, correct);
        String taskId = String.valueOf(emails.hashCode());
        List<RowValue> rows = toRowValues(emails, correctness);
        TaskResult taskResult = new TaskResult(taskId, rows);
        statusPublisher.publishStatusMessages(taskResult);
        return taskId;
    }

    private List<RowValue> toRowValues(List<String> emails, AtomicInteger correctness) {
        return emails.stream()
                .map(e -> new RTEVRowValue(e, toStatus(e, correctness), "..."))
                .collect(Collectors.toList());
    }

    private AtomicInteger createCorrectnessCounter(List<String> emails, BigDecimal correct) {
        BigDecimal correctness = correct.multiply(
                BigDecimal.valueOf(emails.size()))
                .setScale(0, RoundingMode.UP);
        return new AtomicInteger(correctness.intValue());
    }

    private int toStatus(String email, AtomicInteger correctness) {
        if (correctness.getAndDecrement() > 0) {
            return ValidationStatus.OK_VALID_ADDRESS.getCode();
        } else {
            ValidationStatus[] values = ValidationStatus.values();
            int index = Math.abs(email.hashCode()) % values.length;
            return values[index].getCode();
        }
    }
}
