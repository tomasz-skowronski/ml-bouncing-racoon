package de.magicline.racoon.job;

import de.magicline.racoon.config.RacoonMetrics;
import de.magicline.racoon.domain.task.persistance.TaskRepository;

import java.time.Clock;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private static final Logger LOGGER = LogManager.getLogger(JobApplicationService.class);

    private final TaskRepository taskRepository;
    private final Clock clock;
    private final int availabilityDays;

    public JobApplicationService(TaskRepository taskRepository,
                                 Clock clock,
                                 @Value("${task.status.availabilityDays:14}") int availabilityDays) {
        this.taskRepository = taskRepository;
        this.clock = clock;
        this.availabilityDays = availabilityDays;
    }

    @Transactional(readOnly = true)
    public void updateMetrics() {
        long uncompleted = taskRepository.countByModifiedDateIsNull();
        LOGGER.info("uncompleted tasks: {}", uncompleted);
        RacoonMetrics.uncompletedTasks(uncompleted);
    }

    @Transactional
    public void cleanUpTasks() {
        int deleted = taskRepository.deleteAllModifiedBefore(
                clock.instant().minus(availabilityDays, ChronoUnit.DAYS));
        LOGGER.info("{} completed tasks have been deleted", deleted);
    }
}
