package de.magicline.racoon.service.rtev;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RTEVConfiguration;
import de.magicline.racoon.service.taskresult.TaskResult;
import de.magicline.racoon.service.taskresult.ValidationStatus;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ExtendWith({WiremockResolver.class, WiremockUriResolver.class})
class EmailValidationServiceTest {

    private WireMockServer server;
    private EmailValidationService service;
    private RTEVConfiguration rtevConfiguration;

    @BeforeEach
    void setUp(@WiremockResolver.Wiremock WireMockServer server, @WiremockUriResolver.WiremockUri String mockUri) {
        rtevConfiguration = new RTEVConfiguration(
                mockUri,
                mockUri,
                mockUri,
//                RTEVConfiguration.URI_ONE,
//                RTEVConfiguration.URI_ASYNC,
//                RTEVConfiguration.URI_DOWNLOAD,
                "ev-7791b803c271ab303acfa5029b1847e1",
                "https://raccoon.free.beeceptor.com/"
        );
        RTEVValidationClient validationClient = rtevConfiguration.rtevValidationClient();
        this.service = new EmailValidationService(rtevConfiguration, validationClient, new RowsParser());
        this.server = server;
    }

    @Nested
    class validateEmail {

        @Test
        void givenNull() throws JsonProcessingException {
            server.stubFor(post("/api/verify").willReturn(ok()
                    .withBody(json(new RTEVResult(ValidationStatus.INVALID_BAD_ADDRESS)))
            ));

            assertThatThrownBy(() -> service.validateEmail(new ValidateEmailRequest(null)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("400 BAD_REQUEST");
            server.verify(postRequestedFor(urlPathEqualTo("/api/verify"))
                    .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(hasFormParam("APIKey", rtevConfiguration.getApiKey())));
        }

        @Test
        void givenRejectedAddress() throws JsonProcessingException {
            String email = "a@a.pl";
            ValidateEmailRequest request = new ValidateEmailRequest(email);
            server.stubFor(post("/api/verify").willReturn(ok()
                    .withBody(json(new RTEVResult(ValidationStatus.INVALID_ADDRESS_REJECTED)))
//                    .proxiedFrom(RTEVConfiguration.URI_ONE)
            ));

            service.validateEmail(request);

            server.verify(postRequestedFor(urlPathEqualTo("/api/verify"))
                    .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(hasFormParam("EmailAddress", email))
                    .withRequestBody(hasFormParam("APIKey", rtevConfiguration.getApiKey()))
            );
        }

    }

    @Test
    void validateEmailsAsync() throws JsonProcessingException {
        String taskId = "x5-2a6a7d199cc47698f6b8d1cc4995d71d";
        List<String> emails = List.of("a@a.pl", "info@magicline.de");
        ValidateEmailsRequest request = new ValidateEmailsRequest(emails);
        RTEVAsyncResult resposne = new RTEVAsyncResult(ValidationStatus.TASK_ACCEPTED.getCode(), taskId);
        server.stubFor(post("/api/verify").willReturn(ok()
                .withBody(json(resposne))
//                .proxiedFrom(RTEVConfiguration.URI_ASYNC)
        ));

        service.validateEmailsAsync(request);

        server.verify(postRequestedFor(urlPathEqualTo("/api/verify"))
                .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .withRequestBody(hasFormParam("EmailAddress", String.join("\n", emails)))
                .withRequestBody(hasFormParam("APIKey", rtevConfiguration.getApiKey()))
        );
    }

    @Nested
    class downloadTaskResult {

        String taskReport = String.join("\n",
                "email,result,message",
                "a@a.pl,410,Address Rejected",
                "info@magicline.de,200,OK - Valid Address");

        @Test
        void completed() {
            String taskId = "x5-2a6a7d199cc47698f6b8d1cc4995d71d";
            server.stubFor(post("/download.html").willReturn(ok()
                    .withBody(taskReport)
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
//                    .proxiedFrom(RTEVConfiguration.URI_DOWNLOAD)
            ));

            TaskResult report = service.downloadTaskResult(taskId);

            assertThat(report.getTaskId()).isEqualTo(taskId);
            assertThat(report.getRows()).hasSize(2);
            server.verify(postRequestedFor(urlPathEqualTo("/download.html"))
                    .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(hasFormParam("id", taskId))
            );
        }

        @Test
        void notCompleted() {
            String taskId = "invalid-or-not-completed-task-id";
            server.stubFor(post("/download.html").willReturn(ok()
                    .withBody("<!DOCTYPE html><html lang=\"en\"><head></head><body></body>")
                    .withHeader(CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
//                    .proxiedFrom(RTEVConfiguration.URI_DOWNLOAD)
            ));

            assertThatThrownBy(() -> service.downloadTaskResult(taskId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("503 SERVICE_UNAVAILABLE");
            server.verify(postRequestedFor(urlPathEqualTo("/download.html"))
                    .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(hasFormParam("id", taskId))
            );
        }

    }

    private StringValuePattern hasFormParam(String key, String value) {
        return new RegexPattern("(.*&|^)" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8) + "($|&.*)");
    }

    private <T> String json(T responseDto) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(responseDto);
    }
}
