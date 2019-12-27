package de.magicline.racoon.domain.task;

import de.magicline.racoon.domain.provider.EmailValidationService;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.status.StatusPublisher;
import de.magicline.racoon.domain.task.dto.TaskResult;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.web.server.ServerErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TaskListenerTest {

    private static final String TENANT = "tenant1";
    private static final String TASK_ID = "taskId";
    private static final ValidationResult VALIDATION_RESULT = new ValidationResult(List.of());
    private static final TaskResult TASK_RESULT = new TaskResult(TASK_ID, TENANT, VALIDATION_RESULT);

    private TaskListener testee;
    @Mock
    private EmailValidationService emailValidationService;
    @Mock
    private StatusPublisher statusPublisher;

    @BeforeEach
    void setUp() {
        testee = new TaskListener(
                new TasksDownloader(emailValidationService),
                statusPublisher);
    }

    @Nested
    class OnTaskCompleted {

        @Test
        void success() {
            given(emailValidationService.downloadValidationResult(TASK_ID))
                    .willReturn(VALIDATION_RESULT);

            testee.onTaskCompleted(TASK_ID);

            then(statusPublisher).should()
                    .publishStatusMessages(TASK_RESULT);
        }

        @Test
        void failure() {
            given(emailValidationService.downloadValidationResult(TASK_ID))
                    .willThrow(new ServerErrorException("reason", new RuntimeException()));

            assertThatThrownBy(() -> testee.onTaskCompleted(TASK_ID))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);

            then(statusPublisher).shouldHaveNoInteractions();
        }

    }

}
