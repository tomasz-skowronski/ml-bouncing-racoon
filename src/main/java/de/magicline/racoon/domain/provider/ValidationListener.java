package de.magicline.racoon.domain.provider;

import de.magicline.racoon.api.dto.ValidateEmailsRequest;
import de.magicline.racoon.config.RabbitConfiguration;
import de.magicline.racoon.domain.provider.dto.RTEVAsyncResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ValidationListener {

    private static final Logger LOGGER = LogManager.getLogger(ValidationListener.class);

    private final EmailValidationService emailValidationService;

    public ValidationListener(EmailValidationService emailValidationService) {
        this.emailValidationService = emailValidationService;
    }

    @RabbitListener(queues = RabbitConfiguration.VALIDATION_QUEUE)
    public void onValidateEmailsRequest(ValidateEmailsRequest request) {
        try {
            RTEVAsyncResult result = emailValidationService.validateEmailsAsync(request);
            LOGGER.info("onValidateEmailsRequest: {} , taskResult : {}", request, result);
        } catch (Exception e) {
            LOGGER.error("onValidateEmailsRequest FAILED: {}", request, e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
