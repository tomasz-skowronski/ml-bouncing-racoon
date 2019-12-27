package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.ProviderConfiguration;
import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.task.dto.RowValue;
import feign.Response;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailValidationService {

    private final ProviderConfiguration providerConfiguration;
    private final RTEVValidationClient validationClient;
    private final Retry retry;
    private final RowsParser rowsParser;
    private final DataValidator dataValidator;

    public EmailValidationService(ProviderConfiguration providerConfiguration,
                                  RTEVValidationClient validationClient,
                                  RetryConfig retryConfig,
                                  RowsParser rowsParser,
                                  DataValidator dataValidator) {
        this.providerConfiguration = providerConfiguration;
        this.validationClient = validationClient;
        this.retry = Retry.of("rtev", retryConfig);
        this.rowsParser = rowsParser;
        this.dataValidator = dataValidator;
    }

    @PostConstruct
    void init() {
        retry.getEventPublisher().onEvent(RacoonMetrics::incrementValidationRetry);
    }

    public RTEVResult validateEmail(ValidateEmailRequest request) {
        dataValidator.validateRequest(request);
        return retry.executeSupplier(() -> callValidateEmail(request));
    }

    private RTEVResult callValidateEmail(ValidateEmailRequest request) {
        RTEVResult result = validationClient.validateEmail(
                providerConfiguration.getUriOne(),
                providerConfiguration.getApiKey(),
                request.getEmail());
        RacoonMetrics.incrementValidationStatus(result.getStatus());
        return dataValidator.validateResponse(result);
    }

    public RTEVAsyncResult validateEmailsAsync(ValidateEmailsRequest request) {
        dataValidator.validateRequest(request);
        RTEVAsyncResult result = validationClient.validateEmailsAsync(
                providerConfiguration.getUriAsync(),
                providerConfiguration.getApiKey(),
                String.join("\n", request.getEmails()),
                new TaskName(request).generateName(),
                providerConfiguration.getNotifyURL(),
                providerConfiguration.getNotifyEmail());
        return dataValidator.validateResponse(result);
    }

    /**
     * @param taskId an unique identifier
     * @return CSV from https://www.email-validator.net/download.html
     */
    public ValidationResult downloadValidationResult(String taskId) {
        dataValidator.validateNotBlank(taskId);
        Response response = validationClient.downloadTaskResult(
                providerConfiguration.getUriDownload(),
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
