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

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Configuration for Real-Time Email Validation. API provided by Byteplant GmbH.
 */
@Configuration
public class ProviderConfiguration {

    private final ProviderProperties properties;

    public ProviderConfiguration(ProviderProperties properties) {
        this.properties = properties;
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
                .maxAttempts(properties.getRetries().getMaxAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        Duration.ofSeconds(properties.getRetries().getInitialIntervalSec())))
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

}
