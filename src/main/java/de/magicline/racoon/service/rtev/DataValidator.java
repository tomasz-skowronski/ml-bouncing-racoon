package de.magicline.racoon.service.rtev;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.service.task.ValidationStatus;
import feign.Response;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DataValidator {

    private static final int EMAILS_LIMIT = 100_000;
    private static final Map<ValidationStatus, HttpStatus> RESPONSE_ERRORS_MAPPING = Map.of(
            ValidationStatus.INVALID_BAD_ADDRESS, HttpStatus.BAD_REQUEST,
            ValidationStatus.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS,
            ValidationStatus.API_KEY_INVALID_OR_DEPLETED, HttpStatus.FORBIDDEN);

    void validateRequest(ValidateEmailRequest request) {
        validateNotBlank(request.getEmail());
    }

    void validateNotBlank(String content) {
        if (StringUtils.isBlank(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, content);
        }
    }

    void validateRequest(ValidateEmailsRequest request) {
        if (request.getEmails().size() > EMAILS_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "max emails: " + EMAILS_LIMIT);
        }
    }

    <T extends RTEVStatusAware> T validateResponse(T result) {
        ValidationStatus status = ValidationStatus.of(result.getStatus());
        HttpStatus errorStatus = RESPONSE_ERRORS_MAPPING.get(status);
        if (errorStatus == null) {
            return result;
        } else {
            throw new ResponseStatusException(errorStatus);
        }
    }

    void validateResponse(Response result) {
        HttpStatus status = HttpStatus.valueOf(result.status());
        if (status.is3xxRedirection()) {
            String location = getHeader(HttpHeaders.LOCATION, result);
            throw new ResponseStatusException(status, location);
        }
        if (status.is4xxClientError() || status.is5xxServerError()) {
            throw new ResponseStatusException(status);
        }
        String resultContentType = getHeader(HttpHeaders.CONTENT_TYPE, result);
        if (!resultContentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, resultContentType);
        }
    }

    private String getHeader(String name, Response result) {
        return String.join(";",
                result.headers().getOrDefault(name, Collections.emptyList()));
    }
}
