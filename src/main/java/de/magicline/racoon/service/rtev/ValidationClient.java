package de.magicline.racoon.service.rtev;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(name = "notificationClient",
        url = "${app.external-api.url}",
        configuration = RTEVConfiguration.class)
public interface ValidationClient {

    @RequestLine("POST /api/verify")
//    @PostMapping("/api/verify")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    RTEVResponse validate(
            @Param("APIKey") String apiKey,
            @Param("EmailAddress") String emailAddress);

    @RequestLine("POST /api/verify")
//    @PostMapping("/api/verify")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    RTEVResponse validateAsync(
            @Param("APIKey") String apiKey,
            @Param("EmailAddress") String emailAddresses,
            @Param("NotifyURL") String notifyURL);

    @RequestLine("POST /download.html")
//    @PostMapping("/download.html")
    @Headers(value = {
            "Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
            "Accept: application/octet-stream"
    })
    Response downloadTask(
            @Param("id") String id,
            @Param("cmd") String cmd,
            @Param("validaddresses-nocatchall") String validNocatchall,
            @Param("catchalladdresses") String catchall,
            @Param("invalidaddresses") String invalid,
            @Param("suspectaddresses") String suspect,
            @Param("indeterminateaddresses") String indeterminate,
            @Param("no-duplicate-detect") String noDuplicateDetect,
            @Param("output") String output,
            @Param("submit") String submit);

}
