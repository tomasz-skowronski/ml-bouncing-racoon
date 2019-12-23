package de.magicline.racoon.domain.status.dto;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

@SuppressWarnings("unused")
public class StatusMessage {

    private final String taskId;
    private final ValidationStatusDto status;
    private final List<StatusItem> items;

    @ConstructorProperties({"taskId", "status", "items"})
    public StatusMessage(String taskId, ValidationStatusDto status, List<StatusItem> items) {
        this.taskId = taskId;
        this.status = status;
        this.items = items;
    }

    public String getTaskId() {
        return taskId;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public List<StatusItem> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusMessage that = (StatusMessage) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, status, items);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskId", taskId)
                .add("status", status)
                .add("items", items != null ? items.size() : null)
                .toString();
    }
}
