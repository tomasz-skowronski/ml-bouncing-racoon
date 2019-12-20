package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.provider.dto.RTEVStatusAware;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.status.dto.ValidationStatus;
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
            RTEVValidationStatus.INVALID_BAD_ADDRESS, HttpStatus.BAD_REQUEST,
            RTEVValidationStatus.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS,
            RTEVValidationStatus.API_KEY_INVALID_OR_DEPLETED, HttpStatus.PAYMENT_REQUIRED);

    void validateRequest(ValidateEmailRequest request) {
        validateNotBlank(request.getEmail());
        RacoonMetrics.incrementValidations(1);
    }

    void validateNotBlank(String content) {
        if (StringUtils.isBlank(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, content);
        }
    }

    void validateRequest(ValidateEmailsRequest request) {
        int size = request.getEmails().size();
        if (size <= EMAILS_LIMIT) {
            RacoonMetrics.incrementValidations(size);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "max emails: " + EMAILS_LIMIT);
        }
    }

    <T extends RTEVStatusAware> T validateResponse(T result) {
        ValidationStatus resultStatus = RTEVValidationStatus.of(result.getStatus());
        HttpStatus errorStatus = RESPONSE_ERRORS_MAPPING.get(resultStatus);
        if (errorStatus == null) {
            RacoonMetrics.incrementResponseStatus(HttpStatus.OK);
        } else {
            throwResponseStatusException(errorStatus, null);
        }
        return result;
    }

    void validateResponse(Response result) {
        HttpStatus status = HttpStatus.valueOf(result.status());
        if (status.is3xxRedirection()) {
            String location = getHeader(HttpHeaders.LOCATION, result);
            throwResponseStatusException(status, location);
        }
        if (status.is4xxClientError() || status.is5xxServerError()) {
            throwResponseStatusException(status, null);
        }
        String resultContentType = getHeader(HttpHeaders.CONTENT_TYPE, result);
        if (!resultContentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            throwResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, resultContentType);
        }
    }

    private void throwResponseStatusException(HttpStatus status, String reason) {
        RacoonMetrics.incrementResponseStatus(status);
        throw new ResponseStatusException(status, reason);
    }

    private String getHeader(String name, Response result) {
        return String.join(";",
                result.headers().getOrDefault(name, Collections.emptyList()));
    }
}
