package de.magicline.racoon.config;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigurationTest {

    private RabbitConfiguration testee = new RabbitConfiguration(new ObjectMapper());

    @Test
    void declareValidation() {
        Collection<Declarable> result = testee.declareValidation().getDeclarables();

        containsExchanges(result, "ml.racoon");
        containsQueues(result, "ml.racoon.validation");
        containsRoutingKeys(result, "validation");
    }

    @Test
    void declareTask() {
        Collection<Declarable> result = testee.declareTask().getDeclarables();

        containsExchanges(result, "ml.racoon");
        containsQueues(result, "ml.racoon.task");
        containsRoutingKeys(result, "task");
    }

    @Test
    void declareStatus() {
        Collection<Declarable> result = testee.declareStatus().getDeclarables();

        containsExchanges(result, "ml.racoon");
        containsQueues(result, "ml.racoon.status.indeterminate", "ml.racoon.status.invalid", "ml.racoon.status.suspect", "ml.racoon.status.valid");
        containsRoutingKeys(result, "status.valid", "status.indeterminate", "status.invalid", "status.suspect");
    }

    private void containsExchanges(Collection<Declarable> declarables, String... names) {
        assertThat(declarables.stream()
                .filter(d -> d instanceof Exchange)
                .map(d -> ((Exchange) d).getName())
        ).containsExactlyInAnyOrder(names);
    }

    private void containsQueues(Collection<Declarable> declarables, String... names) {
        assertThat(declarables.stream()
                .filter(d -> d instanceof Queue)
                .map(d -> ((Queue) d).getName())
        ).containsExactlyInAnyOrder(names);
    }

    private void containsRoutingKeys(Collection<Declarable> declarables, String... names) {
        assertThat(declarables.stream()
                .filter(d -> d instanceof Binding)
                .map(d -> ((Binding) d).getRoutingKey())
        ).containsExactlyInAnyOrder(names);
    }
}
