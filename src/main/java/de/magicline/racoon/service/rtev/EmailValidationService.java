package de.magicline.racoon.service.rtev;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RTEVConfiguration;
import de.magicline.racoon.service.task.TaskResult;
import de.magicline.racoon.service.task.ValidationStatus;
import feign.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.common.base.Preconditions;

@Service
public class EmailValidationService {

    private static final int EMAILS_LIMIT = 100_000;
    private static final Map<ValidationStatus, HttpStatus> RESPONSE_ERRORS_MAPPING = Map.of(
            ValidationStatus.INVALID_BAD_ADDRESS, HttpStatus.BAD_REQUEST,
            ValidationStatus.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS,
            ValidationStatus.API_KEY_INVALID_OR_DEPLETED, HttpStatus.FORBIDDEN);

    private final RTEVConfiguration rtevConfiguration;
    private final RTEVValidationClient validationClient;
    private final RowsParser rowsParser;

    public EmailValidationService(RTEVConfiguration rtevConfiguration, RTEVValidationClient validationClient, RowsParser rowsParser) {
        this.rtevConfiguration = rtevConfiguration;
        this.validationClient = validationClient;
        this.rowsParser = rowsParser;
    }

    public RTEVResult validateEmail(ValidateEmailRequest request) {
        RTEVResult rtevResponse = validationClient.validateEmail(
                rtevConfiguration.getUriOne(),
                rtevConfiguration.getApiKey(),
                request.getEmail());
        return validateValidationResponse(rtevResponse);
    }

    public RTEVAsyncResult validateEmailsAsync(ValidateEmailsRequest request) {
        RTEVAsyncResult rtevResponse = validationClient.validateEmailsAsync(
                rtevConfiguration.getUriAsync(),
                rtevConfiguration.getApiKey(),
                formatEmails(request.getEmails()),
                rtevConfiguration.getNotifyURL()
        );
        return validateValidationResponse(rtevResponse);
    }

    private String formatEmails(List<String> emails) {
        Preconditions.checkArgument(emails.size() <= EMAILS_LIMIT, "max " + EMAILS_LIMIT);
        return String.join("\n", emails);
    }

    private <T extends StatusAware> T validateValidationResponse(T response) {
        ValidationStatus status = ValidationStatus.of(response.getStatus());
        HttpStatus errorStatus = RESPONSE_ERRORS_MAPPING.get(status);
        if (errorStatus == null) {
            return response;
        } else {
            throw new ResponseStatusException(errorStatus);
        }
    }

    /**
     * @param taskId
     * @return CSV from https://www.email-validator.net/download.html
     */
    public TaskResult downloadTaskResult(String taskId) {
        Response response = validationClient.downloadTaskResult(
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
        validateTaskReportResponse(response);
        try {
            return new TaskResult(taskId,
                    rowsParser.parse(response.body().asInputStream()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "unparsable response", e.getCause());
        }
    }

    private void validateTaskReportResponse(Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        if (status.is3xxRedirection()) {
            String location = getHeader(HttpHeaders.LOCATION, response);
            throw new ResponseStatusException(status, location);
        }
        if (status.is4xxClientError() || status.is5xxServerError()) {
            throw new ResponseStatusException(status);
        }
        String responseContentType = getHeader(HttpHeaders.CONTENT_TYPE, response);
        if (!responseContentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, responseContentType);
        }
    }

    private String getHeader(String name, Response response) {
        return String.join(";",
                response.headers().getOrDefault(name, Collections.emptyList()));
    }
}
