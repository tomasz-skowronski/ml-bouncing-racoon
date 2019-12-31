package de.magicline.racoon.domain.task;

import de.magicline.racoon.common.TestClock;
import de.magicline.racoon.domain.provider.EmailValidationService;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.status.StatusPublisher;
import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.dto.TaskResult;
import de.magicline.racoon.domain.task.persistance.TaskRepository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.web.server.ServerErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
    @Mock
    private TaskRepository taskRepository;
    private Clock clock = new TestClock();

    @BeforeEach
    void setUp() {
        TaskResultFetcher taskResultFetcher = new TaskResultFetcher(taskRepository, emailValidationService, clock);
        testee = new TaskListener(taskResultFetcher, statusPublisher);
    }

    @Nested
    class OnTaskCompleted {

        @Test
        void success() {
            Task task = new Task(TASK_ID, TENANT, clock.instant());
            given(taskRepository.findByTaskId(TASK_ID))
                    .willReturn(Optional.of(task));
            given(emailValidationService.downloadValidationResult(TASK_ID))
                    .willReturn(VALIDATION_RESULT);

            testee.onTaskCompleted(TASK_ID);

            then(taskRepository).should().update(any(Task.class));
            then(statusPublisher).should().publishStatusMessages(TASK_RESULT);
        }

        @Test
        void failureOnDownload() {
            given(emailValidationService.downloadValidationResult(TASK_ID))
                    .willThrow(new ServerErrorException("reason", new RuntimeException()));

            assertThatThrownBy(() -> testee.onTaskCompleted(TASK_ID))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);

            then(taskRepository).shouldHaveNoInteractions();
            then(statusPublisher).shouldHaveNoInteractions();
        }

        @Test
        void failureOnFindByTaskId() {
            given(taskRepository.findByTaskId(TASK_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> testee.onTaskCompleted(TASK_ID))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);

            then(taskRepository).should(never()).update(any(Task.class));
            then(statusPublisher).shouldHaveNoInteractions();
        }

    }

}
