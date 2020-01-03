package de.magicline.racoon.config;

import de.magicline.racoon.api.mock.MockEmailValidator;
import de.magicline.racoon.api.mock.MockService;
import de.magicline.racoon.domain.provider.DataValidator;
import de.magicline.racoon.domain.provider.EmailValidationService;
import de.magicline.racoon.domain.provider.EmailValidator;
import de.magicline.racoon.domain.provider.RTEVValidationClient;
import de.magicline.racoon.domain.provider.RowsParser;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.task.persistance.TaskRepository;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Clock;
import java.time.ZoneId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RacoonApplicationConfiguration {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    private static final Logger LOGGER = LogManager.getLogger(RacoonApplicationConfiguration.class);
    private static final String APP_MOCK_MODE_ENABLED = "app.mockMode.enabled";

    @Bean
    Clock clock() {
        return Clock.system(ZONE_ID);
    }

    /**
     * @param mockService    without validation provider interaction (costless)
     * @param expectedStatus default status
     * @return fake result
     */
    @Bean
    @ConditionalOnProperty(value = APP_MOCK_MODE_ENABLED, havingValue = "true")
    EmailValidator emailValidatorMock(MockService mockService,
                                      @Value("${app.mockMode.expectedStatus}") RTEVValidationStatus expectedStatus) {
        LOGGER.info("MOCK MODE is active (always respond {})", expectedStatus);
        return new MockEmailValidator(mockService, expectedStatus);
    }

    @Bean
    @ConditionalOnProperty(value = APP_MOCK_MODE_ENABLED, havingValue = "false", matchIfMissing = true)
    EmailValidator emailValidator(ProviderProperties providerProperties,
                                  RTEVValidationClient validationClient,
                                  RetryConfig retryConfig,
                                  RowsParser rowsParser,
                                  DataValidator dataValidator,
                                  TaskRepository taskRepository,
                                  Clock clock) {
        return new EmailValidationService(providerProperties, validationClient, retryConfig, rowsParser, dataValidator, taskRepository, clock);
    }

}
