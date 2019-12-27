package de.magicline.racoon.domain.status;

import de.magicline.racoon.domain.provider.dto.RTEVRowValue;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.status.dto.StatusItem;
import de.magicline.racoon.domain.status.dto.StatusMessage;
import de.magicline.racoon.domain.status.dto.ValidationStatusDto;
import de.magicline.racoon.domain.task.dto.TaskResult;

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
    private static final String ROUTING_SUSPECT = "ml.racoon.status.suspect";
    private static final String ROUTING_INVALID = "ml.racoon.status.invalid";

    private StatusPublisher testee;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Captor
    ArgumentCaptor<StatusMessage> suspectCaptor;
    @Captor
    ArgumentCaptor<StatusMessage> invalidCaptor;

    @BeforeEach
    void setUp() {
        testee = new StatusPublisher(
                new TaskResultDispatcher(),
                rabbitTemplate,
                BATCH_SIZE);
    }

    @Test
    void publishToOneRouting() {
        ValidationResult validationResult = new ValidationResult(List.of(
                new RTEVRowValue("a", RTEVValidationStatus.ADDRESS_UNAVAILABLE.getCode(), "m")));
        TaskResult taskResult = new TaskResult("id", "tenant", validationResult);

        testee.publishStatusMessages(taskResult);

        then(rabbitTemplate).should()
                .convertAndSend(eq(ROUTING_SUSPECT), any(StatusMessage.class));
    }

    @Test
    void publishToTwoRoutings() {
        ValidationResult validationResult = new ValidationResult(List.of(
                new RTEVRowValue("c", RTEVValidationStatus.SERVER_UNAVAILABLE.getCode(), "m"),
                new RTEVRowValue("d", RTEVValidationStatus.INVALID_ADDRESS_REJECTED.getCode(), "m")));
        TaskResult taskResult = new TaskResult("id", "tenant", validationResult);

        testee.publishStatusMessages(taskResult);

        then(rabbitTemplate).should()
                .convertAndSend(eq(ROUTING_SUSPECT), suspectCaptor.capture());
        then(rabbitTemplate).should()
                .convertAndSend(eq(ROUTING_INVALID), invalidCaptor.capture());
        assertThat(suspectCaptor.getAllValues())
                .extracting(
                        StatusMessage::getStatus,
                        StatusMessage::getItems)
                .containsExactly(Tuple.tuple(
                        new ValidationStatusDto(RTEVValidationStatus.SERVER_UNAVAILABLE),
                        List.of(new StatusItem("c"))));
        assertThat(invalidCaptor.getAllValues())
                .extracting(
                        StatusMessage::getStatus,
                        StatusMessage::getItems)
                .containsExactly(Tuple.tuple(
                        new ValidationStatusDto(RTEVValidationStatus.INVALID_ADDRESS_REJECTED),
                        List.of(new StatusItem("d"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishMorePartitions() {
        ValidationResult validationResult = new ValidationResult(List.of(
                new RTEVRowValue("a", RTEVValidationStatus.SERVER_UNAVAILABLE.getCode(), "m"),
                new RTEVRowValue("b", RTEVValidationStatus.SERVER_UNAVAILABLE.getCode(), "m"),
                new RTEVRowValue("c", RTEVValidationStatus.SERVER_UNAVAILABLE.getCode(), "m")));
        TaskResult taskResult = new TaskResult("id", "tenant", validationResult);

        testee.publishStatusMessages(taskResult);

        then(rabbitTemplate).should(times(2))
                .convertAndSend(eq(ROUTING_SUSPECT), suspectCaptor.capture());
        assertThat(suspectCaptor.getAllValues())
                .extracting(StatusMessage::getItems)
                .containsExactly(
                        List.of(new StatusItem("a"), new StatusItem("b")),
                        List.of(new StatusItem("c"))
                );
    }

}
