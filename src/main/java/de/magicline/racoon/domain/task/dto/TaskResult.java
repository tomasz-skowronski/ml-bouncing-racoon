package de.magicline.racoon.domain.task.dto;

import de.magicline.racoon.domain.provider.dto.ValidationResult;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class TaskResult {

    private final String taskId;
    private final String tenant;
    private final ValidationResult result;

    public TaskResult(String taskId, String tenant, ValidationResult result) {
        this.taskId = taskId;
        this.tenant = tenant;
        this.result = result;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTenant() {
        return tenant;
    }

    public ValidationResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskId", taskId)
                .add("tenant", tenant)
                .add("result", result)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResult that = (TaskResult) o;
        return taskId.equals(that.taskId) &&
                tenant.equals(that.tenant) &&
                result.equals(that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, tenant, result);
    }
}
