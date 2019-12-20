package de.magicline.racoon.config;

import de.magicline.racoon.domain.provider.dto.RTEVValidationStatus;

import java.util.Arrays;
import java.util.List;
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

    private static final String TASK_ROUTING_KEY = "task";
    private static final String TASK_EXCHANGE = "ml.racoon.callback";
    public static final String TASK_QUEUE = TASK_EXCHANGE + "." + TASK_ROUTING_KEY;
    private static final String TASK_DLX = TASK_EXCHANGE + ".dlx";
    private static final String TASK_DLQ = TASK_QUEUE + ".dlq";
    private static final String STATUS_EXCHANGE = "ml.racoon.status";

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
    public Declarables declareTask() {
        return new Declarables(
                ExchangeBuilder.directExchange(TASK_EXCHANGE).build(),
                QueueBuilder.durable(TASK_QUEUE).withArgument("x-dead-letter-exchange", RabbitConfiguration.TASK_DLX).build(),
                new Binding(TASK_QUEUE, QUEUE, TASK_EXCHANGE, TASK_ROUTING_KEY, null));
    }

    @Bean
    public Declarables declareTaskDL() {
        return new Declarables(
                ExchangeBuilder.directExchange(TASK_DLX).build(),
                QueueBuilder.durable(TASK_DLQ).build(),
                new Binding(TASK_DLQ, QUEUE, TASK_DLX, TASK_ROUTING_KEY, null));
    }

    @Bean
    public Declarables declareStatus() {
        List<Declarable> items = Stream.concat(
                Stream.of(ExchangeBuilder.directExchange(STATUS_EXCHANGE).build()),
                Arrays.stream(RTEVValidationStatus.Type.values())
                        .flatMap(this::createQueueAndBinding)
        ).collect(Collectors.toList());
        return new Declarables(items);
    }

    private Stream<Declarable> createQueueAndBinding(RTEVValidationStatus.Type type) {
        String name = toStatusQueueName(type);
        return Stream.of(
                QueueBuilder.durable(name).build(),
                new Binding(name, QUEUE, STATUS_EXCHANGE, name, null)
        );
    }

    public static String toStatusQueueName(RTEVValidationStatus.Type type) {
        return STATUS_EXCHANGE + "." + type.name().toLowerCase();
    }

}
