package de.magicline.racoon.api;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailResponse;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.api.dto.ValidateEmailsResponse;
import de.magicline.racoon.domain.provider.EmailValidator;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/racoon/validation")
public class EmailsValidationController {

    private final EmailValidator emailValidator;

    public EmailsValidationController(EmailValidator emailValidator) {
        this.emailValidator = emailValidator;
    }

    @PostMapping
    public ValidateEmailResponse validateEmail(
            @Valid @RequestBody ValidateEmailRequest request) {
        RTEVResult result = emailValidator.validateEmail(request);
        return new ValidateEmailResponse(result);
    }

    @PostMapping("/async")
    public ResponseEntity<ValidateEmailsResponse> validateEmailsAsync(
            @Valid @RequestBody ValidateEmailsRequest request) {
        RTEVAsyncResult result = emailValidator.validateEmailsAsync(request);
        return ResponseEntity.accepted()
                .body(new ValidateEmailsResponse(result));
    }

}
