package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RabbitConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class ValidationListenerIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @MockBean
    private EmailValidationService emailValidationService;
    private final AtomicBoolean received = new AtomicBoolean();

    @Test
    void onValidateEmailsRequest() {
        ValidateEmailsRequest request = new ValidateEmailsRequest(List.of(), "tenant", null);
        given(emailValidationService.validateEmailsAsync(request)).willAnswer(inv -> {
            received.set(true);
            return null;
        });

        rabbitTemplate.convertAndSend(RabbitConfiguration.RACOON_EXCHANGE, RabbitConfiguration.VALIDATION_ROUTING_KEY, request);

        await().atMost(ofSeconds(2)).untilAtomic(received, is(true));
    }
}
