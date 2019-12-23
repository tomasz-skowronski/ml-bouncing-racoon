package de.magicline.racoon.domain.task;

import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.domain.provider.RTEVValidationClient;
import de.magicline.racoon.domain.provider.dto.RTEVRowValue;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.status.dto.StatusItem;
import de.magicline.racoon.domain.status.dto.StatusMessage;
import de.magicline.racoon.domain.task.dto.RowValue;
import feign.Request;
import feign.Response;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static de.magicline.racoon.common.SerializationHelper.toCsv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class TaskCallbacksServiceIT {

    private static final String TASK_ID = "some task id";
    private static final RTEVValidationStatus STATUS = RTEVValidationStatus.OK_VALID_ADDRESS;
    private static final String STATUS_VALID_QUEUE = "ml.racoon.status.valid";

    @Autowired
    private TaskCallbacksService testee;
    @MockBean
    private RTEVValidationClient validationClient;
    @Autowired
    private TaskListener taskListener;
    private AtomicReference<StatusMessage> receivedStatusMessage = new AtomicReference<>();

    @Test
    void shouldPublishStatusMessageWhenTaskCompleted() throws IOException {
        RTEVRowValue row = new RTEVRowValue("email", STATUS.getCode(), "message");
        given(validationClient
                .downloadTaskResult(any(URI.class), eq(TASK_ID), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(toResponse(toCsv(RowValue.class, row)));

        testee.complete(TASK_ID);

        await().atMost(2, TimeUnit.SECONDS).untilAtomic(receivedStatusMessage, notNullValue());
        assertThat(receivedStatusMessage.get().getItems())
                .extracting(StatusItem::getEmail)
                .containsExactly("email");
        assertThat(receivedStatusMessage.get())
                .extracting(
                        m -> m.getStatus().getCode(),
                        m -> m.getStatus().getType(),
                        StatusMessage::getTaskId,
                        m -> m.getItems().size())
                .containsExactly(
                        STATUS.getCode(),
                        STATUS.getType(),
                        TASK_ID,
                        1);
    }

    private Response toResponse(String body) {
        return Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "url", Map.of(), Request.Body.empty()))
                .headers(Map.of(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                .body(body, StandardCharsets.UTF_8)
                .status(200)
                .build();
    }

    @RabbitListener(queues = RabbitConfiguration.TASK_QUEUE)
    void onTaskCompleted(String taskId) {
        taskListener.onTaskCompleted(taskId);
    }

    @RabbitListener(queues = STATUS_VALID_QUEUE)
    void onValid(StatusMessage statusMessage) {
        receivedStatusMessage.set(statusMessage);
    }

}
