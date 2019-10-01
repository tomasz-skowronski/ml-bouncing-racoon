package de.magicline.racoon.service.rtev;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ExtendWith({WiremockResolver.class, WiremockUriResolver.class})
class EmailValidationServiceTest {

    // TODO:
    private static final String RTVE_URL_1 = "https://api.email-validator.net";
    private static final String RTVE_URL_2 = "https://bulk.email-validator.net";
    private static final String RTVE_URL_3 = "https://www.email-validator.net";
    private static final String PATH_API_VERIFY = "/api/verify";
    private static final String PATH_DOWNLOAD = "/download.html";

    private WireMockServer server;
    private EmailValidationService service;

    @BeforeEach
    void setUp(@WiremockResolver.Wiremock WireMockServer server, @WiremockUriResolver.WiremockUri String uri) {
//        uri  = RTVE_URL_3; // TODO
        ValidationClient validationClient = Feign.builder()
                .encoder(new FormEncoder())
                .decoder(new JacksonDecoder())
                .retryer(Retryer.NEVER_RETRY)
                .options(new Request.Options(10000, 60000, false))
                .logger(new Slf4jLogger())
                .logLevel(feign.Logger.Level.FULL)
                .target(ValidationClient.class, uri);
        this.service = new EmailValidationService(uri, validationClient, new RowsParser());
        this.server = server;
    }

    @Test
    void validateNull() throws JsonProcessingException {
        server.stubFor(post(PATH_API_VERIFY).willReturn(ok()
                        .withBody(json(new RTEVResponse(RTEVStatus.INVALID_BAD_ADDRESS)))
//                .proxiedFrom(RTVE_URL_1)
        ));

        service.validate("");

        server.verify(postRequestedFor(urlPathEqualTo(PATH_API_VERIFY))
                .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .withRequestBody(hasFormParam("EmailAddress", ""))
                .withRequestBody(hasFormParam("APIKey", EmailValidationService.APIKey)));
    }

    @Test
    void validateOneAddress() throws JsonProcessingException {
        String email = "a@a.pl";
        server.stubFor(post(PATH_API_VERIFY).willReturn(ok()
                        .withBody(json(new RTEVResponse(RTEVStatus.INVALID_ADDRESS_REJECTED)))
//                .proxiedFrom(RTVE_URL_1)
        ));

        service.validate(email);

        server.verify(postRequestedFor(urlPathEqualTo(PATH_API_VERIFY))
                .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .withRequestBody(hasFormParam("EmailAddress", email))
                .withRequestBody(hasFormParam("APIKey", EmailValidationService.APIKey))
        );
    }

    @Test
    void validateBulkAddresses() throws JsonProcessingException {
        String[] emails = {"a@a.pl",
                "info@magicline.de"};
        String taskId = "x5-2a6a7d199cc47698f6b8d1cc4995d71d";
        server.stubFor(post(PATH_API_VERIFY).willReturn(ok()
                        .withBody(json(new RTEVResponse(RTEVStatus.TASK_ACCEPTED.getCode(), taskId, null)))
//                .proxiedFrom(RTVE_URL_2)
        ));

        service.validateAsync(emails);

        server.verify(postRequestedFor(urlPathEqualTo(PATH_API_VERIFY))
                .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .withRequestBody(hasFormParam("EmailAddress", String.join("\n", emails)))
                .withRequestBody(hasFormParam("APIKey", EmailValidationService.APIKey))
        );
    }

    @Test
    void downloadTaskResult() {
        String taskResult = String.join("\n",
                "email,result,message",
                "a@a.pl,410,Address Rejected",
                "info@magicline.de,200,OK - Valid Address");
        String taskId = "x5-2a6a7d199cc47698f6b8d1cc4995d71d";
        server.stubFor(post(PATH_DOWNLOAD).willReturn(ok()
                        .withBody(taskResult)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
//                .proxiedFrom(RTVE_URL_3)
        ));

        List<RowValue> rows = service.downloadTaskResult(taskId);

        assertThat(rows).hasSize(2);
        server.verify(postRequestedFor(urlPathEqualTo(PATH_DOWNLOAD))
                .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .withRequestBody(hasFormParam("id", taskId))
        );
    }

    private StringValuePattern hasFormParam(String key, String value) {
        return new RegexPattern("(.*&|^)" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8) + "($|&.*)");
    }

    private String json(RTEVResponse responseDto) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(responseDto);
    }
}
