package de.magicline.racoon.domain.task;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.common.ProviderPropertiesBuilder;
import de.magicline.racoon.common.TestClock;
import de.magicline.racoon.config.ProviderConfiguration;
import de.magicline.racoon.config.ProviderProperties;
import de.magicline.racoon.domain.provider.DataValidator;
import de.magicline.racoon.domain.provider.EmailValidationService;
import de.magicline.racoon.domain.provider.RowsParser;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationMode;
import de.magicline.racoon.domain.status.dto.StatusItem;
import de.magicline.racoon.domain.status.dto.StatusMessage;
import de.magicline.racoon.domain.status.dto.ValidationStatusDto;
import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.persistance.TaskRepository;
import io.github.resilience4j.retry.RetryConfig;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MockServiceIT {

    private static final Logger LOGGER = LogManager.getLogger(MockServiceIT.class);

    private EmailValidationService testee;
    @LocalServerPort
    private int port;
    private final ConcurrentLinkedQueue<StatusMessage> receivedSuspects = new ConcurrentLinkedQueue<>();
    private CountDownLatch count;
    @MockBean
    private TaskRepository taskRepository;
    private Clock clock = new TestClock();

    @BeforeEach
    void setUp() {
        URI uri = URI.create("http://localhost:" + port + "/racoon/mock");
        ProviderProperties providerProperties = ProviderPropertiesBuilder.builder()
                .withUri(new ProviderProperties.Uris(uri, uri, uri))
                .build();
        ProviderConfiguration providerConfiguration = new ProviderConfiguration(providerProperties);
        this.testee = new EmailValidationService(
                providerProperties,
                providerConfiguration.rtevValidationClient(),
                RetryConfig.ofDefaults(),
                new RowsParser(),
                new DataValidator(),
                taskRepository,
                clock);
    }

    @Test
    void validateUsingMockAndRetrieveStatusMessage() throws InterruptedException {
        ValidateEmailsRequest request = new ValidateEmailsRequest(
                List.of("1@a.pl", "2@a.pl", "3@a.pl", "4@a.pl", "5@a.pl"),
                "tenant", ValidationMode.EXPRESS);
        String taskId = "-392623883";
        count = new CountDownLatch(request.getEmails().size());

        RTEVAsyncResult response = testee.validateEmailsAsync(request);

        assertThat(response.getInfo()).isEqualTo(taskId);
        count.await(2, TimeUnit.SECONDS);
        assertThat(count.getCount()).isZero();
        assertThat(receivedSuspects).contains(
                new StatusMessage(taskId,
                        "tenant",
                        new ValidationStatusDto(RTEVValidationStatus.LOCAL_ADDRESS),
                        List.of(new StatusItem("2@a.pl"))),
                new StatusMessage(taskId,
                        "tenant",
                        new ValidationStatusDto(RTEVValidationStatus.DISPOSABLE_ADDRESS),
                        List.of(new StatusItem("5@a.pl")))
        );
        then(taskRepository).should().insert(any(Task.class));
    }

    @RabbitListener(queues = "ml.racoon.status.suspect")
    void onSuspect(StatusMessage statusMessage) {
        onStatusMessage(statusMessage);
        receivedSuspects.add(statusMessage);
    }

    @RabbitListener(queues = "ml.racoon.status.valid")
    void onValid(StatusMessage statusMessage) {
        onStatusMessage(statusMessage);
    }

    @RabbitListener(queues = "ml.racoon.status.invalid")
    void onInvalid(StatusMessage statusMessage) {
        onStatusMessage(statusMessage);
    }

    @RabbitListener(queues = "ml.racoon.status.indeterminate")
    void onIndeterminate(StatusMessage statusMessage) {
        onStatusMessage(statusMessage);
    }

    private void onStatusMessage(StatusMessage statusMessage) {
        LOGGER.debug(statusMessage);
        count.countDown();
    }

}
