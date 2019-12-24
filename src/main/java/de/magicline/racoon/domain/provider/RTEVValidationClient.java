package de.magicline.racoon.domain.provider;

import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

import java.net.URI;

public interface RTEVValidationClient {

    @RequestLine("POST /api/verify")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    RTEVResult validateEmail(
            URI uri,
            @Param("APIKey") String apiKey,
            @Param("EmailAddress") String emailAddress);

    @RequestLine("POST /api/verify")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    RTEVAsyncResult validateEmailsAsync(
            URI uri,
            @Param("APIKey") String apiKey,
            @Param("EmailAddress") String emailAddresses,
            @Param("NotifyURL") String notifyURL,
            @Param("NotifyEmail") String notifyEmail);

    @RequestLine("POST /download.html")
    @Headers(value = {
            "Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
            "Accept: application/octet-stream"
    })
    @SuppressWarnings("squid:S00107")
    Response downloadTaskResult(
            URI uri,
            @Param("id") String taskId,
            @Param("cmd") String command,
            @Param("validaddresses-nocatchall") String validFilter,
            @Param("catchalladdresses") String catchallFilter,
            @Param("invalidaddresses") String invalidFilter,
            @Param("suspectaddresses") String suspectFilter,
            @Param("indeterminateaddresses") String indeterminateFilter,
            @Param("output") String csvFormat);

}
