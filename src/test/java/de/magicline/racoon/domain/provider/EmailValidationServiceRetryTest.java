package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.config.ProviderConfiguration;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EmailValidationServiceRetryTest {

    private static final int MAX_ATTEMPTS = 2;
    private EmailValidationService testee;
    private ProviderConfiguration providerConfiguration = new ProviderConfiguration(
            "", "", "", "", "", null, MAX_ATTEMPTS, 1);
    private DataValidator dataValidator = new DataValidator();
    @Mock
    private RTEVValidationClient validationClient;
    @Mock
    private RowsParser rowsParser;

    @BeforeEach
    void setUp() {
        testee = new EmailValidationService(providerConfiguration,
                validationClient,
                providerConfiguration.retryConfig(),
                rowsParser,
                dataValidator);
    }

    @Test
    void validateEmailFailing() {
        String email = "email";
        ValidateEmailRequest request = new ValidateEmailRequest(email);
        RTEVResult rateLimitExceeded = new RTEVResult(RTEVValidationStatus.RATE_LIMIT_EXCEEDED);
        given(validationClient.validateEmail(any(), any(), any()))
                .willReturn(rateLimitExceeded);

        assertThatThrownBy(() -> testee.validateEmail(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("429 TOO_MANY_REQUESTS");

        then(validationClient).should(times(MAX_ATTEMPTS))
                .validateEmail(any(), any(), any());
    }
}
