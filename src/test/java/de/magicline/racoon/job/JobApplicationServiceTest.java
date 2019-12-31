package de.magicline.racoon.job;

import de.magicline.racoon.common.TestClock;
import de.magicline.racoon.domain.task.persistance.TaskRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    private JobApplicationService testee;
    @Mock
    private TaskRepository taskRepository;
    private Clock clock = new TestClock();
    private int availabilityDays = 2;

    @BeforeEach
    void setUp() {
        testee = new JobApplicationService(taskRepository, clock, availabilityDays);
    }

    @Test
    void cleanUpTasks() {
        Instant today = clock.instant();
        Instant twoDaysAgo = today.minus(availabilityDays, ChronoUnit.DAYS);

        testee.cleanUpTasks();

        then(taskRepository).should().deleteAllModifiedBefore(twoDaysAgo);
        assertThat(Duration.between(twoDaysAgo, today)).isEqualTo(Duration.ofDays(2));
    }
}
