package de.magicline.racoon.service.rtev;

import feign.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class EmailValidationService {

    private static final Logger LOGGER = LogManager.getLogger(EmailValidationService.class);
    static final String APIKey = "ev-7791b803c271ab303acfa5029b1847e1";
    private static final String RACCOON_FREE_BEECEPTOR_COM = "https://raccoon.free.beeceptor.com/";

    private final String uri;
    private final ValidationClient validationClient;
    private final String notifyURL = RACCOON_FREE_BEECEPTOR_COM;
    private final RowsParser rowsParser;

    public EmailValidationService(String uri, ValidationClient validationClient, RowsParser rowsParser) {
        this.uri = uri;
        this.validationClient = validationClient;
        this.rowsParser = rowsParser;
    }

    public RTEVResponse validate(String mail) {
        return validationClient.validate(APIKey, mail);
    }

    public RTEVResponse validateAsync(String... mails) {
        return validationClient.validateAsync(APIKey,
                String.join("\n", mails),
                notifyURL
        );
    }

    public List<RowValue> downloadTaskResult(String taskId) {
        Response response = validationClient.downloadTask(
                taskId,
                "download",
                "valid-nocatchall",
                "catchall",
                "invalid",
                "suspect",
                "indeterminate",
                "no-duplicate-detect",
                "long",
                "submit"
        );
        validateTaskResult(response);
        try {
            return rowsParser.parse(response.body().asInputStream());
        } catch (IOException e) {
            throw new RTEVException(RTEVException.Error.RESPONSE_CONTENT, e);
        }
    }

    private void validateTaskResult(Response response) {
        if (response.status() == 302) {
            String location = getHeader(HttpHeaders.LOCATION, response);
            throw new RTEVException(RTEVException.Error.RESPONSE_REDIRECT, location);
        }
        String contentType = getHeader(HttpHeaders.CONTENT_TYPE, response);
        if (!contentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            throw new RTEVException(RTEVException.Error.RESPONSE_CONTENT_TYPE, contentType);
        }
    }

    private String getHeader(String name, Response response) {
        return String.join(";",
                response.headers().getOrDefault(name, Collections.emptyList()));
    }
}
