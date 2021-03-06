package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.common.ProviderPropertiesBuilder;
import de.magicline.racoon.common.TestClock;
import de.magicline.racoon.config.ProviderConfiguration;
import de.magicline.racoon.config.ProviderProperties;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationMode;
import de.magicline.racoon.domain.provider.dto.ValidationResult;
import de.magicline.racoon.domain.task.dto.Task;
import de.magicline.racoon.domain.task.persistance.TaskRepository;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.net.URI;
import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static de.magicline.racoon.common.HttpHelper.hasFormParam;
import static de.magicline.racoon.common.SerializationHelper.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ExtendWith({MockitoExtension.class, WiremockResolver.class, WiremockUriResolver.class})
class EmailValidationServiceTest {

    private WireMockServer server;
    private EmailValidationService service;
    @Mock
    private TaskRepository taskRepository;
    private Clock clock = new TestClock();
    private ProviderProperties providerProperties;

    @BeforeEach
    void setUp(@WiremockResolver.Wiremock WireMockServer server,
               @WiremockUriResolver.WiremockUri String mockUri) {
        URI uri = URI.create(mockUri);
        providerProperties = ProviderPropertiesBuilder.builder()
                .withUri(new ProviderProperties.Uris(uri, uri, uri))
                .build();

        ProviderConfiguration providerConfiguration = new ProviderConfiguration(providerProperties);
        RTEVValidationClient validationClient = providerConfiguration.rtevValidationClient();
        this.service = new EmailValidationService(
                providerProperties,
                validationClient,
                providerConfiguration.retryConfig(),
                new RowsParser(),
                new DataValidator(),
                taskRepository,
                clock);
        this.server = server;
    }

    @Nested
    class validateEmail {

        @Test
        void givenRejectedAddress() throws JsonProcessingException {
            String email = "a@a.pl";
            ValidateEmailRequest request = new ValidateEmailRequest(email);
            server.stubFor(post("/api/verify").willReturn(ok()
                    .withBody(toJson(new RTEVResult(RTEVValidationStatus.INVALID_ADDRESS_REJECTED)))
            ));

            service.validateEmail(request);

            server.verify(postRequestedFor(urlPathEqualTo("/api/verify")));
        }

        @Test
        void givenThrottling() throws JsonProcessingException {
            String email = "a@a.pl";
            ValidateEmailRequest request = new ValidateEmailRequest(email);
            server.stubFor(post("/api/verify").willReturn(ok()
                    .withBody(toJson(new RTEVResult(RTEVValidationStatus.RATE_LIMIT_EXCEEDED)))
            ));

            assertThatThrownBy(() -> service.validateEmail(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessage("429 TOO_MANY_REQUESTS");
            server.verify(2, postRequestedFor(urlPathEqualTo("/api/verify")));
        }

    }

    @Nested
    class validateEmailsAsync {

        String taskId = "x5-2a6a7d199cc47698f6b8d1cc4995d71d";
        List<String> emails = List.of("a@a.pl", "info@magicline.de");
        ValidateEmailsRequest request = new ValidateEmailsRequest(emails, "tenant", ValidationMode.EXPRESS);

        @Test
        void success() throws JsonProcessingException {
            RTEVAsyncResult response = new RTEVAsyncResult(RTEVValidationStatus.TASK_ACCEPTED.getCode(), taskId);
            server.stubFor(post("/api/verify").willReturn(ok()
                    .withBody(toJson(response))
            ));

            service.validateEmailsAsync(request);

            server.verify(postRequestedFor(urlPathEqualTo("/api/verify"))
                    .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(hasFormParam("EmailAddress", String.join("\n", emails)))
                    .withRequestBody(hasFormParam("APIKey", providerProperties.getApiKey()))
            );
            then(taskRepository).should().insert(any(Task.class));
        }

        @Test
        void failure() {
            server.stubFor(post("/api/verify").willReturn(status(503).withBody("...")));

            assertThatThrownBy(() -> service.validateEmailsAsync(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessage("503 SERVICE_UNAVAILABLE \"...\"");
            server.verify(postRequestedFor(urlPathEqualTo("/api/verify")));
            then(taskRepository).shouldHaveNoInteractions();
        }

        @Nested
        class downloadTaskResult {

            String taskResult = String.join("\n",
                    "email,result,message",
                    "a@a.pl,410,Address Rejected",
                    "info@magicline.de,200,OK - Valid Address");

            @Test
            void completed() {
                String taskId = "x5-2a6a7d199cc47698f6b8d1cc4995d71d";
                server.stubFor(post("/download.html").willReturn(ok()
                        .withBody(taskResult)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                ));

                ValidationResult result = service.downloadValidationResult(taskId);

                assertThat(result.getRows()).hasSize(2);
                server.verify(postRequestedFor(urlPathEqualTo("/download.html"))
                        .withHeader(CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                        .withRequestBody(hasFormParam("id", taskId))
                );
            }

            @Test
            void notCompletedProbably() {
                String taskId = "invalid-or-not-completed-task-id";
                server.stubFor(post("/download.html").willReturn(ok()
                        .withBody("<!DOCTYPE html><html lang=\"en\"><head></head><body></body>")
                        .withHeader(CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                ));

                assertThatThrownBy(() -> service.downloadValidationResult(taskId))
                        .isInstanceOf(ResponseStatusException.class)
                        .hasMessageStartingWith("503 SERVICE_UNAVAILABLE \"text/html\"");
                server.verify(postRequestedFor(urlPathEqualTo("/download.html")));
            }

            @Test
            void serverError() {
                String taskId = "invalid-or-not-completed-task-id";
                server.stubFor(post("/download.html").willReturn(serviceUnavailable()));

                assertThatThrownBy(() -> service.downloadValidationResult(taskId))
                        .isInstanceOf(ResponseStatusException.class)
                        .hasMessageStartingWith("503 SERVICE_UNAVAILABLE");
                server.verify(postRequestedFor(urlPathEqualTo("/download.html")));
            }

        }
    }
}
