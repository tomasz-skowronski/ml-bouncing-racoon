package de.magicline.racoon.domain.provider.dto;

import de.magicline.racoon.domain.task.dto.RowValue;

import java.util.List;

public final class ValidationResult {

    private final List<RowValue> rows;

    public ValidationResult(List<RowValue> rows) {
        this.rows = rows;
    }

    public List<RowValue> getRows() {
        return rows;
    }
}
