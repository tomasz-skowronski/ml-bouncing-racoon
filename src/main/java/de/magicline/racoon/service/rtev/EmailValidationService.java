package de.magicline.racoon.service.rtev;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RTEVConfiguration;
import de.magicline.racoon.service.task.RowValue;
import de.magicline.racoon.service.task.TaskResult;
import feign.Response;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailValidationService {

    private final RTEVConfiguration rtevConfiguration;
    private final RTEVValidationClient validationClient;
    private final Retry retry;
    private final RowsParser rowsParser;
    private final DataValidator dataValidator;

    public EmailValidationService(RTEVConfiguration rtevConfiguration, RTEVValidationClient validationClient, RetryConfig retryConfig, RowsParser rowsParser, DataValidator dataValidator) {
        this.rtevConfiguration = rtevConfiguration;
        this.validationClient = validationClient;
        this.retry = Retry.of("rtev", retryConfig);
        this.rowsParser = rowsParser;
        this.dataValidator = dataValidator;
    }

    public RTEVResult validateEmail(ValidateEmailRequest request) {
        return retry.executeSupplier(() -> callValidateEmail(request));
    }

    private RTEVResult callValidateEmail(ValidateEmailRequest request) {
        dataValidator.validateRequest(request);
        RTEVResult result = validationClient.validateEmail(
                rtevConfiguration.getUriOne(),
                rtevConfiguration.getApiKey(),
                request.getEmail());
        return dataValidator.validateResponse(result);
    }

    public RTEVAsyncResult validateEmailsAsync(ValidateEmailsRequest request) {
        dataValidator.validateRequest(request);
        RTEVAsyncResult result = validationClient.validateEmailsAsync(
                rtevConfiguration.getUriAsync(),
                rtevConfiguration.getApiKey(),
                String.join("\n", request.getEmails()),
                rtevConfiguration.getNotifyURL()
        );
        return dataValidator.validateResponse(result);
    }

    /**
     * @param taskId an unique identifier
     * @return CSV from https://www.email-validator.net/download.html
     */
    public TaskResult downloadTaskResult(String taskId) {
        dataValidator.validateNotBlank(taskId);
        Response result = validationClient.downloadTaskResult(
                rtevConfiguration.getUriDownload(),
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
        dataValidator.validateResponse(result);
        try {
            return new TaskResult(taskId, parseBody(result));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "unparsable result", e.getCause());
        }
    }

    private List<RowValue> parseBody(Response result) throws IOException {
        return rowsParser.parse(result.body().asInputStream());
    }
}
