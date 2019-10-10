package de.magicline.racoon.service.task;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RTEVConfiguration;
import de.magicline.racoon.service.rtev.EmailValidationService;
import de.magicline.racoon.service.rtev.RTEVAsyncResult;
import de.magicline.racoon.service.rtev.RTEVValidationClient;
import de.magicline.racoon.service.rtev.RowsParser;
import de.magicline.racoon.service.status.StatusItem;
import de.magicline.racoon.service.status.StatusMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MockServiceIT {

    private EmailValidationService testee;
    @LocalServerPort
    private int port;
    private final AtomicReference<StatusMessage> received = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        String uri = "http://localhost:" + port + "/racoon/mock";
        RTEVConfiguration rtevConfiguration = new RTEVConfiguration(uri, uri, uri, "unused", "unused");
        RTEVValidationClient validationClient = rtevConfiguration.rtevValidationClient();
        this.testee = new EmailValidationService(rtevConfiguration, validationClient, new RowsParser());
    }

    @Test
    void validateUsingMockAndRetrieveStatusMessage() {
        ValidateEmailsRequest request = new ValidateEmailsRequest(
                List.of("1@a.pl", "2@a.pl", "3@a.pl", "4@a.pl", "5@a.pl"));

        RTEVAsyncResult response = testee.validateEmailsAsync(request);

        assertThat(response.getInfo()).isEqualTo("-392623883");
        await().atMost(2, TimeUnit.SECONDS).until(() -> received.get() != null);
        StatusMessage message = received.get();
        assertThat(message.getTaskId()).isEqualTo("-392623883");
        assertThat(message.getStatus()).isEqualTo("suspect");
        assertThat(message.getItems()).containsExactly(
                new StatusItem("2@a.pl", 302),
                new StatusItem("5@a.pl", 305));
    }

    @RabbitListener(queues = "ml.racoon.status.suspect")
    void onSuspectStatusMessage(StatusMessage statusMessage) {
        received.set(statusMessage);
    }

}
