package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.ValidationResult;

public interface EmailValidator {

    RTEVResult validateEmail(ValidateEmailRequest request);

    RTEVAsyncResult validateEmailsAsync(ValidateEmailsRequest request);

    ValidationResult downloadValidationResult(String taskId);
}
