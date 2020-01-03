package de.magicline.racoon.api.mock;

import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.RTEVRowValue;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.status.StatusPublisher;
import de.magicline.racoon.domain.task.dto.RowValue;
import de.magicline.racoon.domain.task.dto.TaskResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class MockService {

    private static final String INFO = "MOCK";

    private final StatusPublisher statusPublisher;

    public MockService(StatusPublisher statusPublisher) {
        this.statusPublisher = statusPublisher;
    }

    RTEVAsyncResult validateAsExpected(List<String> emails, String tenant, RTEVValidationStatus expectedStatus) {
        ValidationResult result = new ValidationResult(emails.stream()
                .map(e -> new RTEVRowValue(e, expectedStatus.getCode(), INFO))
                .collect(Collectors.toList()));
        return answer(tenant, tenant, result);
    }

    RTEVResult validate(String email) {
        return new RTEVResult(toStatus(email, 0));
    }

    RTEVAsyncResult validate(List<String> emails, String tenant, BigDecimal correct) {
        AtomicInteger correctness = createCorrectnessCounter(emails, correct);
        String taskId = String.valueOf(emails.hashCode());
        ValidationResult result = new ValidationResult(toRows(emails, correctness));
        return answer(tenant, taskId, result);
    }

    private List<RowValue> toRows(List<String> emails, AtomicInteger correctness) {
        return emails.stream()
                .map(e -> {
                    int status = toStatus(e, correctness.getAndDecrement()).getCode();
                    return new RTEVRowValue(e, status, INFO);
                })
                .collect(Collectors.toList());
    }

    private AtomicInteger createCorrectnessCounter(List<String> emails, BigDecimal correct) {
        BigDecimal correctness = correct.multiply(
                BigDecimal.valueOf(emails.size()))
                .setScale(0, RoundingMode.UP);
        return new AtomicInteger(correctness.intValue());
    }

    private RTEVValidationStatus toStatus(String email, int correctness) {
        if (correctness > 0) {
            return RTEVValidationStatus.OK_VALID_ADDRESS;
        } else {
            RTEVValidationStatus[] values = RTEVValidationStatus.values();
            int index = Math.abs(email.hashCode()) % values.length;
            return values[index];
        }
    }

    private RTEVAsyncResult answer(String tenant, String taskId, ValidationResult result) {
        statusPublisher.publishStatusMessages(new TaskResult(taskId, tenant, result));
        return new RTEVAsyncResult(RTEVValidationStatus.TASK_ACCEPTED.getCode(), taskId);
    }
}
