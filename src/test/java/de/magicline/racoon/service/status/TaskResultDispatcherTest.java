package de.magicline.racoon.service.status;

import de.magicline.racoon.service.rtev.RTEVRowValue;
import de.magicline.racoon.service.rtev.RTEVValidationStatus;
import de.magicline.racoon.service.task.TaskResult;
import de.magicline.racoon.service.task.ValidatedEmail;

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
        TaskResult taskResult = new TaskResult("id", List.of(
                new RTEVRowValue("a", RTEVValidationStatus.INVALID_ADDRESS_UNAVAILABLE.getCode(), "m"),
                new RTEVRowValue("b", RTEVValidationStatus.INVALID_ADDRESS_REJECTED.getCode(), "m")));

        Map<ValidationStatus, List<ValidatedEmail>> result = testee.dispatch(taskResult);

        assertThat(result).containsOnlyKeys(
                RTEVValidationStatus.INVALID_ADDRESS_UNAVAILABLE,
                RTEVValidationStatus.INVALID_ADDRESS_REJECTED);
    }

    @Test
    void dispatchValid() {
        TaskResult taskResult = new TaskResult("id", List.of(
                new RTEVRowValue("a", RTEVValidationStatus.OK_VALID_ADDRESS.getCode(), "m"),
                new RTEVRowValue("b", RTEVValidationStatus.OK_CATCH_ALL_ACTIVE.getCode(), "m"),
                new RTEVRowValue("c", RTEVValidationStatus.OK_CATCH_ALL_TEST_DELAYED.getCode(), "m"),
                new RTEVRowValue("d", RTEVValidationStatus.OK_CATCH_ALL_TEST_DELAYED.getCode(), "m")));

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
