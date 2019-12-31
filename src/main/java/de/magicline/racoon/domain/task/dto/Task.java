package de.magicline.racoon.domain.task.dto;

import java.time.Instant;
import java.util.Objects;

import com.google.common.base.MoreObjects;

@SuppressWarnings("unused")
public final class Task {

    private final String taskId;
    private final String tenant;
    private final Instant createdDate;
    private final Instant modifiedDate;

    public Task(String taskId, String tenant, Instant createdDate) {
        this(taskId, tenant, createdDate, null);
    }

    public Task(String taskId, String tenant, Instant createdDate, Instant modifiedDate) {
        this.taskId = taskId;
        this.tenant = tenant;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public Task withModifiedDate(Instant instant) {
        return new Task(taskId, tenant, createdDate, instant);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTenant() {
        return tenant;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskId", taskId)
                .add("tenant", tenant)
                .add("createdDate", createdDate)
                .add("modifiedDate", modifiedDate)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId.equals(task.taskId) &&
                tenant.equals(task.tenant) &&
                Objects.equals(createdDate, task.createdDate) &&
                Objects.equals(modifiedDate, task.modifiedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, tenant, createdDate, modifiedDate);
    }
}
