package de.magicline.racoon.domain.status;

import de.magicline.racoon.domain.provider.dto.RTEVRowValue;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.status.dto.ValidationStatus;
import de.magicline.racoon.domain.task.dto.TaskResult;
import de.magicline.racoon.domain.task.dto.ValidatedEmail;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TaskResultDispatcherTest {

    private TaskResultDispatcher testee = new TaskResultDispatcher();

    @Test
    void dispatchInvalid() {
        ValidationResult validationResult = new ValidationResult(List.of(
                new RTEVRowValue("a", RTEVValidationStatus.INVALID_ADDRESS_UNAVAILABLE.getCode(), "m"),
                new RTEVRowValue("b", RTEVValidationStatus.INVALID_ADDRESS_REJECTED.getCode(), "m")));
        TaskResult taskResult = new TaskResult("id", "tenant", validationResult);

        Map<ValidationStatus, List<ValidatedEmail>> result = testee.dispatch(taskResult);

        assertThat(result).containsOnlyKeys(
                RTEVValidationStatus.INVALID_ADDRESS_UNAVAILABLE,
                RTEVValidationStatus.INVALID_ADDRESS_REJECTED);
    }

    @Test
    void dispatchValid() {
        ValidationResult validationResult = new ValidationResult(List.of(
                new RTEVRowValue("a", RTEVValidationStatus.OK_VALID_ADDRESS.getCode(), "m"),
                new RTEVRowValue("b", RTEVValidationStatus.OK_CATCH_ALL_ACTIVE.getCode(), "m"),
                new RTEVRowValue("c", RTEVValidationStatus.OK_CATCH_ALL_TEST_DELAYED.getCode(), "m"),
                new RTEVRowValue("d", RTEVValidationStatus.OK_CATCH_ALL_TEST_DELAYED.getCode(), "m")));
        TaskResult taskResult = new TaskResult("id", "tenant", validationResult);

        Map<ValidationStatus, List<ValidatedEmail>> result = testee.dispatch(taskResult);

        assertThat(result).hasSize(3);
        assertThat(result.get(RTEVValidationStatus.OK_VALID_ADDRESS))
                .hasSize(1);
        assertThat(result.get(RTEVValidationStatus.OK_CATCH_ALL_ACTIVE))
                .hasSize(1);
        assertThat(result.get(RTEVValidationStatus.OK_CATCH_ALL_TEST_DELAYED))
                .hasSize(2);
    }

}
