package de.magicline.racoon.service.task;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RTEVConfiguration;
import de.magicline.racoon.service.rtev.DataValidator;
import de.magicline.racoon.service.rtev.EmailValidationService;
import de.magicline.racoon.service.rtev.RTEVAsyncResult;
import de.magicline.racoon.service.rtev.RTEVValidationClient;
import de.magicline.racoon.service.rtev.RTEVValidationStatus;
import de.magicline.racoon.service.rtev.RowsParser;
import de.magicline.racoon.service.status.StatusItem;
import de.magicline.racoon.service.status.StatusMessage;
import de.magicline.racoon.service.status.ValidationStatusDto;
import io.github.resilience4j.retry.RetryConfig;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MockServiceIT {

    private EmailValidationService testee;
    @LocalServerPort
    private int port;
    private final ConcurrentLinkedQueue<StatusMessage> receivedSuspects = new ConcurrentLinkedQueue<>();
    private CountDownLatch count;

    @BeforeEach
    void setUp() {
        String uri = "http://localhost:" + port + "/racoon/mock";
        RTEVConfiguration rtevConfiguration = new RTEVConfiguration(uri, uri, uri, "unused", "unused", 1, 1);
        RTEVValidationClient validationClient = rtevConfiguration.rtevValidationClient();
        this.testee = new EmailValidationService(rtevConfiguration,
                validationClient,
                RetryConfig.ofDefaults(),
                new RowsParser(),
                new DataValidator());
    }

    @Test
    void validateUsingMockAndRetrieveStatusMessage() throws InterruptedException {
        ValidateEmailsRequest request = new ValidateEmailsRequest(
                List.of("1@a.pl", "2@a.pl", "3@a.pl", "4@a.pl", "5@a.pl"));
        String taskId = "-392623883";
        count = new CountDownLatch(request.getEmails().size());

        RTEVAsyncResult response = testee.validateEmailsAsync(request);

        assertThat(response.getInfo()).isEqualTo(taskId);
        count.await(2, TimeUnit.SECONDS);
        assertThat(count.getCount()).isZero();
        assertThat(receivedSuspects).contains(
                new StatusMessage(taskId,
                        new ValidationStatusDto(RTEVValidationStatus.LOCAL_ADDRESS),
                        List.of(new StatusItem("2@a.pl"))),
                new StatusMessage(taskId,
                        new ValidationStatusDto(RTEVValidationStatus.DISPOSABLE_ADDRESS),
                        List.of(new StatusItem("5@a.pl")))
        );
    }

    @RabbitListener(queues = "ml.racoon.status.suspect")
    void onSuspect(StatusMessage statusMessage) {
        count.countDown();
        receivedSuspects.add(statusMessage);
    }

    @RabbitListener(queues = "ml.racoon.status.valid")
    void onValid(StatusMessage statusMessage) {
        count.countDown();
    }

    @RabbitListener(queues = "ml.racoon.status.invalid")
    void onInvalid(StatusMessage statusMessage) {
        count.countDown();
    }

    @RabbitListener(queues = "ml.racoon.status.indeterminate")
    void onIndeterminate(StatusMessage statusMessage) {
        count.countDown();
    }

}
