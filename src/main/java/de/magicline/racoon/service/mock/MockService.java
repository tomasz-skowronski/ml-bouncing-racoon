package de.magicline.racoon.service.mock;

import de.magicline.racoon.service.rtev.RTEVAsyncResult;
import de.magicline.racoon.service.rtev.RTEVResult;
import de.magicline.racoon.service.rtev.RTEVRowValue;
import de.magicline.racoon.service.rtev.RTEVValidationStatus;
import de.magicline.racoon.service.status.StatusPublisher;
import de.magicline.racoon.service.task.RowValue;
import de.magicline.racoon.service.task.TaskResult;

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

    public RTEVResult validate(String email) {
        return new RTEVResult(toStatus(email, 0));
    }

    public RTEVAsyncResult validate(List<String> emails, BigDecimal correct) {
        AtomicInteger correctness = createCorrectnessCounter(emails, correct);
        String taskId = String.valueOf(emails.hashCode());
        List<RowValue> rows = toRowValues(emails, correctness);
        statusPublisher.publishStatusMessages(new TaskResult(taskId, rows));
        return new RTEVAsyncResult(RTEVValidationStatus.TASK_ACCEPTED.getCode(), taskId);
    }

    private List<RowValue> toRowValues(List<String> emails, AtomicInteger correctness) {
        return emails.stream()
                .map(e -> {
                    int status = toStatus(e, correctness.getAndDecrement()).getCode();
                    return new RTEVRowValue(e, status, "...");
                })
                .collect(Collectors.toList());
    }

    private AtomicInteger createCorrectnessCounter(List<String> emails, BigDecimal correct) {
        BigDecimal correctness = correct.multiply(
                BigDecimal.valueOf(emails.size()))
                .setScale(0, RoundingMode.UP);
        return new AtomicInteger(correctness.intValue());
    }

    private RTEVValidationStatus toStatus(String email, Integer correctness) {
        if (correctness > 0) {
            return RTEVValidationStatus.OK_VALID_ADDRESS;
        } else {
            RTEVValidationStatus[] values = RTEVValidationStatus.values();
            int index = Math.abs(email.hashCode()) % values.length;
            return values[index];
        }
    }
}
