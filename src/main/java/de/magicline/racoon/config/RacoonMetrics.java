package de.magicline.racoon.config;

import io.github.resilience4j.retry.event.RetryEvent;
import io.micrometer.core.instrument.Metrics;

import java.time.Duration;

import org.springframework.http.HttpStatus;

public final class RacoonMetrics {

    private RacoonMetrics() {
        super();
    }

    public static void incrementResponseStatus(HttpStatus httpStatus) {
        Metrics.counter("racoon.response.status",
                "status", String.valueOf(httpStatus.value())
        ).increment();
    }

    public static void incrementValidationStatus(Integer validationStatus) {
        Metrics.counter("racoon.validation.status",
                "status", validationStatus.toString()
        ).increment();
    }

    public static void incrementValidations(int count) {
        Metrics.counter("racoon.validations.quantity")
                .increment(count);
    }

    public static void durationOfTask(Duration duration) {
        Metrics.timer("racoon.tasks.duration")
                .record(duration);
    }

    public static void incrementValidationRetry(RetryEvent count) {
        Metrics.counter("racoon.validation.retry",
                "event", count.getClass().getSimpleName()
        ).increment();
    }

}
