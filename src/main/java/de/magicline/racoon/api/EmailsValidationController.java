package de.magicline.racoon.api;

import de.magicline.racoon.api.dto.ValidateEmailRequest;
import de.magicline.racoon.api.dto.ValidateEmailResponse;
import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.api.dto.ValidateEmailsResponse;
import de.magicline.racoon.service.rtev.EmailValidationService;
import de.magicline.racoon.service.rtev.RTEVAsyncResult;
import de.magicline.racoon.service.rtev.RTEVResult;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/racoon/validation")
public class EmailsValidationController {

    private final EmailValidationService emailValidationService;

    public EmailsValidationController(EmailValidationService emailValidationService) {
        this.emailValidationService = emailValidationService;
    }

    @PostMapping
    public ValidateEmailResponse validateEmail(
            @Valid @RequestBody ValidateEmailRequest request) {
        RTEVResult result = emailValidationService.validateEmail(request);
        return new ValidateEmailResponse(result);
    }

    @PostMapping("/async")
    public ResponseEntity<ValidateEmailsResponse> validateEmailsAsync(
            @Valid @RequestBody ValidateEmailsRequest request) {
        RTEVAsyncResult result = emailValidationService.validateEmailsAsync(request);
        return ResponseEntity.accepted()
                .body(new ValidateEmailsResponse(result));
    }

}
