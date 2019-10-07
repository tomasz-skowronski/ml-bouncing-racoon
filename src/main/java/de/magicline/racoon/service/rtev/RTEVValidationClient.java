package de.magicline.racoon.service.rtev;

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
            @Param("NotifyURL") String notifyURL);

    @RequestLine("POST /download.html")
    @Headers(value = {
            "Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
            "Accept: application/octet-stream"
    })
    Response downloadTaskResult(
            URI uri,
            @Param("id") String taskId,
            @Param("cmd") String command,
            @Param("validaddresses-nocatchall") String validFilter,
            @Param("catchalladdresses") String catchallFilter,
            @Param("invalidaddresses") String invalidFilter,
            @Param("suspectaddresses") String suspectFilter,
            @Param("indeterminateaddresses") String indeterminateFilter,
            @Param("output") String csvFormat,
            @Param("submit") String submit);

}
