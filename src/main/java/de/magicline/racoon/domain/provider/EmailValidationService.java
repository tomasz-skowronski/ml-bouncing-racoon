package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.ProviderConfiguration;
import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.task.dto.RowValue;
import de.magicline.racoon.domain.task.dto.TaskResult;
import feign.Response;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import javax.annotation.PostConstruct;

import java.io.IOException;
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
    void init(){
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
                providerConfiguration.getNotifyURL()
        );
        return dataValidator.validateResponse(result);
    }

    /**
     * @param taskId an unique identifier
     * @return CSV from https://www.email-validator.net/download.html
     */
    public TaskResult downloadTaskResult(String taskId) {
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
                "long",
                "submit"
        );
        dataValidator.validateResponse(response);
        try {
            return new TaskResult(taskId, parseBody(response));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "unparsable", e);
        }
    }

    private List<RowValue> parseBody(Response response) throws IOException {
        return rowsParser.parse(response.body().asInputStream());
    }
}
