package de.magicline.racoon.domain.task;

import de.magicline.racoon.domain.provider.EmailValidationService;
import de.magicline.racoon.domain.status.StatusPublisher;
import de.magicline.racoon.domain.task.dto.TaskResult;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.web.server.ServerErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TaskListenerTest {

    @InjectMocks
    private TaskListener testee;
    @Mock
    private EmailValidationService emailValidationService;
    @Mock
    private StatusPublisher statusPublisher;

    @Nested
    class OnTaskCompleted {

        private String taskId = "taskId";
        private TaskResult taskResult = new TaskResult(taskId, List.of());

        @Test
        void success() {
            given(emailValidationService.downloadTaskResult(taskId))
                    .willReturn(taskResult);

            testee.onTaskCompleted(taskId);

            then(statusPublisher).should()
                    .publishStatusMessages(taskResult);
        }

        @Test
        void failure() {
            given(emailValidationService.downloadTaskResult(taskId))
                    .willThrow(new ServerErrorException("reason", new RuntimeException()));

            assertThatThrownBy(() -> testee.onTaskCompleted(taskId))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);

            then(statusPublisher).shouldHaveNoInteractions();
        }

    }

}
