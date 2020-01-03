package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.ProviderProperties;
import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.task.dto.RowValue;
import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.persistance.TaskRepository;
import feign.Response;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

public class EmailValidationService implements EmailValidator {

    private final ProviderProperties providerProperties;
    private final RTEVValidationClient validationClient;
    private final Retry retry;
    private final RowsParser rowsParser;
    private final DataValidator dataValidator;
    private final TaskRepository taskRepository;
    private final Clock clock;

    public EmailValidationService(ProviderProperties providerProperties,
                                  RTEVValidationClient validationClient,
                                  RetryConfig retryConfig,
                                  RowsParser rowsParser,
                                  DataValidator dataValidator,
                                  TaskRepository taskRepository,
                                  Clock clock) {
        this.providerProperties = providerProperties;
        this.validationClient = validationClient;
        this.retry = Retry.of("rtev", retryConfig);
        this.rowsParser = rowsParser;
        this.dataValidator = dataValidator;
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    @PostConstruct
    void init() {
        retry.getEventPublisher().onEvent(RacoonMetrics::incrementValidationRetry);
    }

    @Override
    public RTEVResult validateEmail(ValidateEmailRequest request) {
        dataValidator.validateRequest(request);
        return retry.executeSupplier(() -> callValidateEmail(request));
    }

    private RTEVResult callValidateEmail(ValidateEmailRequest request) {
        RTEVResult result = validationClient.validateEmail(
                providerProperties.getUris().getSync(),
                providerProperties.getApiKey(),
                request.getEmail());
        RacoonMetrics.incrementValidationStatus(result.getStatus());
        return dataValidator.validateResponse(result);
    }

    @Transactional
    @Override
    public RTEVAsyncResult validateEmailsAsync(ValidateEmailsRequest request) {
        dataValidator.validateRequest(request);
        RTEVAsyncResult result = validationClient.validateEmailsAsync(
                providerProperties.getUris().getAsync(),
                providerProperties.getApiKey(),
                String.join("\n", request.getEmails()),
                getValidationMode(request),
                new TaskName(request).generate(clock),
                providerProperties.getNotifyURL(),
                providerProperties.getNotifyEmail());
        dataValidator.validateResponse(result);
        createTask(request, result);
        return result;
    }

    private String getValidationMode(ValidateEmailsRequest request) {
        return Objects.requireNonNullElse(
                request.getValidationMode(),
                providerProperties.getValidationMode()
        ).getValue();
    }

    private void createTask(ValidateEmailsRequest request, RTEVAsyncResult result) {
        taskRepository.insert(
                new Task(result.getInfo(), request.getTenant(), clock.instant()));
    }

    /**
     * @param taskId an unique identifier
     * @return CSV from https://www.email-validator.net/download.html
     */
    @Override
    public ValidationResult downloadValidationResult(String taskId) {
        dataValidator.validateNotBlank(taskId);
        Response response = validationClient.downloadTaskResult(
                providerProperties.getUris().getResults(),
                taskId,
                "download",
                "valid-nocatchall",
                "catchall",
                "invalid",
                "suspect",
                "indeterminate",
                "long");
        return new ValidationResult(read(response));
    }

    private List<RowValue> read(Response response) {
        dataValidator.validateResponse(response);
        try (InputStream is = response.body().asInputStream()) {
            return rowsParser.parse(is);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "unparsable", e);
        }
    }
}
