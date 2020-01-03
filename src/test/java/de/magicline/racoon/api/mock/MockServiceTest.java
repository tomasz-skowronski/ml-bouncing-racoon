package de.magicline.racoon.api.mock;

import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVRowValue;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.status.StatusPublisher;
import de.magicline.racoon.domain.task.dto.TaskResult;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;


@ExtendWith(MockitoExtension.class)
class MockServiceTest {

    private static final String TENANT = "tenant";
    private static final String MESSAGE = "MOCK";
    @InjectMocks
    private MockService testee;
    @Mock
    private StatusPublisher statusPublisher;

    @Test
    void validateAsExpected() {
        List<String> emails = List.of("a", "b");
        RTEVValidationStatus expectedStatus = RTEVValidationStatus.ADDRESS_UNAVAILABLE;

        RTEVAsyncResult result = testee.validateAsExpected(emails, TENANT, expectedStatus);

        then(statusPublisher).should().publishStatusMessages(new TaskResult(
                TENANT,
                TENANT,
                new ValidationResult(List.of(
                        new RTEVRowValue("a", expectedStatus.getCode(), MESSAGE),
                        new RTEVRowValue("b", expectedStatus.getCode(), MESSAGE)))));
        assertThat(result).extracting(
                RTEVAsyncResult::getStatus,
                RTEVAsyncResult::getInfo
        ).containsExactly(
                RTEVValidationStatus.TASK_ACCEPTED.getCode(),
                TENANT);
    }

}
