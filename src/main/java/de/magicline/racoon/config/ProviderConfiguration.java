package de.magicline.racoon.config;

import de.magicline.racoon.domain.provider.RTEVValidationClient;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;

import java.net.URI;
import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Configuration for Real-Time Email Validation. API provided by Byteplant GmbH.
 */
@Configuration
public class ProviderConfiguration {

    private final URI uriOne;
    private final URI uriAsync;
    private final URI uriDownload;
    private String notifyURL;
    private String notifyEmail;
    private String apiKey;
    private int retryMaxAttempts;
    private long retryInitialIntervalSec;

    public ProviderConfiguration(
            @Value("${app.rtev.uri.one}") String uriOne,
            @Value("${app.rtev.uri.async}") String uriAsync,
            @Value("${app.rtev.uri.download}") String uriDownload,
            @Value("${app.rtev.notifyURL}") String notifyURL,
            @Value("${app.rtev.notifyEmail}") String notifyEmail,
            @Value("${app.rtev.apiKey}") String apiKey,
            @Value("${app.rtev.retry.maxAttempts}") int retryMaxAttempts,
            @Value("${app.rtev.retry.initialIntervalSec}") long retryInitialIntervalSec) {
        this.uriOne = URI.create(uriOne);
        this.uriAsync = URI.create(uriAsync);
        this.uriDownload = URI.create(uriDownload);
        this.notifyURL = notifyURL;
        this.notifyEmail = notifyEmail;
        this.apiKey = apiKey;
        this.retryMaxAttempts = retryMaxAttempts;
        this.retryInitialIntervalSec = retryInitialIntervalSec;
    }

    @Bean
    public RTEVValidationClient rtevValidationClient() {
        return Feign.builder()
                .encoder(new FormEncoder())
                .decoder(new JacksonDecoder())
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder(errorDecoder())
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .target(RTEVValidationClient.class, "ignore it");
    }

    private ErrorDecoder errorDecoder() {
        return (methodKey, response) -> new ResponseStatusException(
                HttpStatus.valueOf(response.status()),
                StringUtils.substring(response.body().toString(), 0, 100)
        );
    }

    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        Duration.ofSeconds(retryInitialIntervalSec)))
                .retryOnException(this::isRetryExpected)
                .build();
    }

    private boolean isRetryExpected(Throwable throwable) {
        if (throwable instanceof ResponseStatusException) {
            ResponseStatusException e = (ResponseStatusException) throwable;
            return e.getStatus().equals(HttpStatus.TOO_MANY_REQUESTS)
                    || e.getStatus().is5xxServerError();
        } else {
            return false;
        }
    }

    public URI getUriOne() {
        return uriOne;
    }

    public URI getUriAsync() {
        return uriAsync;
    }

    public URI getUriDownload() {
        return uriDownload;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getNotifyURL() {
        return notifyURL;
    }

    public String getNotifyEmail() {
        return notifyEmail;
    }
}
