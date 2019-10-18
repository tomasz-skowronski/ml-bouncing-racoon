package de.magicline.racoon.api;


import de.magicline.racoon.service.mock.MockService;
import de.magicline.racoon.service.rtev.RTEVAsyncResult;
import de.magicline.racoon.service.rtev.RTEVValidationStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Preconditions;

@RestController
@RequestMapping("/racoon/mock")
public class MockController {

    private final MockService mockService;

    public MockController(MockService mockService) {
        this.mockService = mockService;
    }

    /**
     * @param correct expected minimum level of correctness (valid e-mails)
     * @param params  form urlencoded values
     * @return emails quantity, statusId
     */
    @PostMapping(value = "/api/verify",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RTEVAsyncResult> verify(@RequestParam(defaultValue = "0.2") BigDecimal correct,
                                                  @RequestParam Map<String, String> params) {
        List<String> emails = parseEmails(params);
        String statusId = mockService.validate(emails, correct);
        return ResponseEntity.accepted()
                .header("emails", String.valueOf(emails.size()))
                .body(new RTEVAsyncResult(RTEVValidationStatus.TASK_ACCEPTED.getCode(), statusId));
    }

    private List<String> parseEmails(Map<String, String> params) {
        String emailAddress = params.get("EmailAddress");
        Preconditions.checkArgument(emailAddress != null, "no EmailAddress");
        return Arrays.asList(emailAddress.split("\n"));
    }

}
