package de.magicline.racoon.service.task;

import java.util.List;

public final class TaskResult {

    private final String taskId;
    private final List<RowValue> rows;

    public TaskResult(String taskId, List<RowValue> rows) {
        this.taskId = taskId;
        this.rows = rows;
    }

    public String getTaskId() {
        return taskId;
    }

    public List<RowValue> getRows() {
        return rows;
    }
}
