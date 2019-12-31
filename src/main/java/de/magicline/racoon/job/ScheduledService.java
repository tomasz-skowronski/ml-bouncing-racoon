package de.magicline.racoon.job;

import javax.management.timer.Timer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

    private final JobApplicationService jobApplicationService;

    public ScheduledService(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @Scheduled(initialDelay = Timer.ONE_MINUTE, fixedDelayString = "${app.job.delay.metrics}")
    public void updateMetrics() {
        jobApplicationService.updateMetrics();
    }

    @Scheduled(initialDelay = Timer.ONE_MINUTE, fixedDelayString = "${app.job.delay.cleanup}")
    public void cleanUpTasks() {
        jobApplicationService.cleanUpTasks();
    }
}
