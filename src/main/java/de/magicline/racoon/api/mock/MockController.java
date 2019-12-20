package de.magicline.racoon.api.mock;


import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;
import de.magicline.racoon.domain.provider.dto.RTEVResult;
import de.magicline.racoon.domain.provider.dto.RTEVStatusAware;

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

    private static final String DELIMITER = "\n";
    private final MockService mockService;

    public MockController(MockService mockService) {
        this.mockService = mockService;
    }

    /**
     * @param correct expected minimum level of correctness (valid e-mails)
     * @param params  form urlencoded EmailAddress - one or more (sync/async)
     * @return taskId (async) or validation status (sync)
     */
    @PostMapping(value = "/api/verify",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RTEVStatusAware> verify(@RequestParam(defaultValue = "0.2") BigDecimal correct,
                                                  @RequestParam Map<String, String> params) {
        if (isAsyncExpected(params)) {
            return ResponseEntity.accepted().body(verifyAsync(params, correct));
        } else {
            return ResponseEntity.ok(verifySync(params));
        }
    }

    private boolean isAsyncExpected(Map<String, String> params) {
        return getEmails(params).contains(DELIMITER)
                || params.containsKey("NotifyURL")
                || params.containsKey("NotifyEmail");
    }

    private String getEmails(Map<String, String> params) {
        String emailAddress = params.get("EmailAddress");
        Preconditions.checkArgument(emailAddress != null, "no EmailAddress");
        return emailAddress;
    }

    private RTEVAsyncResult verifyAsync(Map<String, String> params, BigDecimal correct) {
        List<String> emails = Arrays.asList(getEmails(params).split(DELIMITER));
        return mockService.validate(emails, correct);
    }

    private RTEVResult verifySync(@RequestParam Map<String, String> params) {
        return mockService.validate(getEmails(params));
    }

}
