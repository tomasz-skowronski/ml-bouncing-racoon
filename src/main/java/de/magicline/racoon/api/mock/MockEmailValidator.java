package de.magicline.racoon.api.mock;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.domain.provider.EmailValidator;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;
import de.magicline.racoon.domain.provider.dto.ValidationResult;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MockEmailValidator implements EmailValidator {

    private static final String INFO = "MOCK";

    private final MockService mockService;
    private final RTEVValidationStatus expectedStatus;

    public MockEmailValidator(MockService mockService,
                              RTEVValidationStatus expectedStatus) {
        this.mockService = mockService;
        this.expectedStatus = expectedStatus;
    }

    @Override
    public RTEVResult validateEmail(ValidateEmailRequest request) {
        return new RTEVResult(expectedStatus.getCode(), expectedStatus.name(), INFO, 0, 0);
    }

    @Override
    public RTEVAsyncResult validateEmailsAsync(ValidateEmailsRequest request) {
        return mockService.validateAsExpected(request.getEmails(), request.getTenant(), expectedStatus);
    }

    @Override
    public ValidationResult downloadValidationResult(String taskId) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "MOCK");
    }
}
