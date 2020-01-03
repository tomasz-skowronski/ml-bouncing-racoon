package de.magicline.racoon.config;

import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.amqp.core.Binding.DestinationType.QUEUE;

@Configuration
@EnableRabbit
public class RabbitConfiguration {

    // TODO update ml-external
    public static final String RACOON_EXCHANGE = "ml.racoon";
    private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    private static final String DLX = ".dlx";
    private static final String DLQ = ".dlq";

    public static final String VALIDATION_ROUTING_KEY = "validation";
    public static final String VALIDATION_QUEUE = RACOON_EXCHANGE + "." + VALIDATION_ROUTING_KEY;
    public static final String TASK_ROUTING_KEY = "task";
    public static final String TASK_QUEUE = RACOON_EXCHANGE + "." + TASK_ROUTING_KEY;

    private final ObjectMapper jackson2ObjectMapper;

    public RabbitConfiguration(ObjectMapper jackson2ObjectMapper) {
        this.jackson2ObjectMapper = jackson2ObjectMapper;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(jackson2ObjectMapper);
    }

    @Bean
    public RabbitAdmin admin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }

    @Bean
    public Declarables declareValidation() {
        return createDeclarablesWithBinding(RACOON_EXCHANGE,
                VALIDATION_QUEUE,
                Map.of(X_DEAD_LETTER_EXCHANGE, RACOON_EXCHANGE + DLX),
                VALIDATION_ROUTING_KEY);
    }

    @Bean
    public Declarables declareValidationDeadLetter() {
        return createDeclarablesWithBinding(RACOON_EXCHANGE + DLX,
                VALIDATION_QUEUE + DLQ,
                Map.of(),
                VALIDATION_ROUTING_KEY);
    }

    @Bean
    public Declarables declareTask() {
        return createDeclarablesWithBinding(RACOON_EXCHANGE,
                TASK_QUEUE,
                Map.of(X_DEAD_LETTER_EXCHANGE, RACOON_EXCHANGE + DLX),
                TASK_ROUTING_KEY);
    }

    @Bean
    public Declarables declareTaskDeadLetter() {
        return createDeclarablesWithBinding(RACOON_EXCHANGE + DLX,
                TASK_QUEUE + DLQ,
                Map.of(),
                TASK_ROUTING_KEY);
    }

    private Declarables createDeclarablesWithBinding(String exchange, String queue, Map<String, Object> queueArgs, String routingKey) {
        return new Declarables(
                ExchangeBuilder.directExchange(exchange).build(),
                QueueBuilder.durable(queue).withArguments(queueArgs).build(),
                new Binding(queue, QUEUE, exchange, routingKey, Map.of()));
    }

    @Bean
    public Declarables declareStatus() {
        return new Declarables(Stream.concat(
                Stream.of(ExchangeBuilder.directExchange(RACOON_EXCHANGE).build()),
                Arrays.stream(RTEVValidationStatus.Type.values())
                        .flatMap(this::createQueueAndBinding)
        ).collect(Collectors.toList()));
    }

    private Stream<Declarable> createQueueAndBinding(RTEVValidationStatus.Type type) {
        String queueName = RACOON_EXCHANGE + "." + toRoutingKey(type);
        return Stream.of(
                QueueBuilder.durable(queueName).build(),
                new Binding(queueName, QUEUE, RACOON_EXCHANGE, toRoutingKey(type), Map.of()));
    }

    public static String toRoutingKey(RTEVValidationStatus.Type type) {
        return "status." + type.name().toLowerCase();
    }

}
