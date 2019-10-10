package de.magicline.racoon.service.task;

import de.magicline.racoon.service.rtev.RTEVRowValue;
import de.magicline.racoon.service.status.StatusItem;
import de.magicline.racoon.service.status.StatusMessage;
import de.magicline.racoon.service.status.StatusPublisher;

import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class StatusPublisherTest {

    private static final int BATCH_SIZE = 2;

    private StatusPublisher testee;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Captor
    ArgumentCaptor<StatusMessage> validCaptor;
    @Captor
    ArgumentCaptor<StatusMessage> invalidCaptor;

    @BeforeEach
    void setUp() {
        testee = new StatusPublisher(rabbitTemplate, BATCH_SIZE);
    }

    @Test
    void publishOneItem() {
        TaskResult taskResult = new TaskResult("id", List.of(
                new RTEVRowValue("a", ValidationStatus.OK_CATCH_ALL_ACTIVE.getCode(), "m")));

        testee.publishStatusMessages(taskResult);

        then(rabbitTemplate).should()
                .convertAndSend(eq("ml.racoon.status.valid"), any(StatusMessage.class));
    }

    @Test
    void publishTwoTypes() {
        TaskResult taskResult = new TaskResult("id", List.of(
                new RTEVRowValue("c", ValidationStatus.OK_CATCH_ALL_TEST_DELAYED.getCode(), "m"),
                new RTEVRowValue("d", ValidationStatus.INVALID_ADDRESS_REJECTED.getCode(), "m")));

        testee.publishStatusMessages(taskResult);

        then(rabbitTemplate).should()
                .convertAndSend(eq("ml.racoon.status.valid"), validCaptor.capture());
        then(rabbitTemplate).should()
                .convertAndSend(eq("ml.racoon.status.invalid"), invalidCaptor.capture());
        assertThat(validCaptor.getAllValues())
                .extracting(
                        StatusMessage::getStatus,
                        StatusMessage::getItems)
                .containsExactly(Tuple.tuple(
                        "valid",
                        List.of(new StatusItem("c", 215))));
        assertThat(invalidCaptor.getAllValues())
                .extracting(StatusMessage::getStatus, StatusMessage::getItems)
                .containsExactly(Tuple.tuple(
                        "invalid",
                        List.of(new StatusItem("d", 410))));
    }

    @Test
    void publishMorePartitions() {
        TaskResult taskResult = new TaskResult(
                "id", List.of(
                new RTEVRowValue("a", ValidationStatus.OK_VALID_ADDRESS.getCode(), "m"),
                new RTEVRowValue("b", ValidationStatus.OK_CATCH_ALL_ACTIVE.getCode(), "m"),
                new RTEVRowValue("c", ValidationStatus.OK_CATCH_ALL_TEST_DELAYED.getCode(), "m")));

        testee.publishStatusMessages(taskResult);

        then(rabbitTemplate).should(times(2))
                .convertAndSend(eq("ml.racoon.status.valid"), validCaptor.capture());
        assertThat(validCaptor.getAllValues())
                .extracting(StatusMessage::getStatus, sm -> sm.getItems().size())
                .containsExactly(
                        Tuple.tuple("valid", 2),
                        Tuple.tuple("valid", 1));
        assertThat(validCaptor.getAllValues().stream()
                .flatMap(sm -> sm.getItems().stream()))
                .containsExactly(
                        new StatusItem("a", 200),
                        new StatusItem("b", 207),
                        new StatusItem("c", 215));
    }

}
