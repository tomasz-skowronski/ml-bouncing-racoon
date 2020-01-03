package de.magicline.racoon.domain.provider.dto;

import de.magicline.racoon.domain.task.dto.RowValue;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class ValidationResult {

    private final List<RowValue> rows;

    public ValidationResult(List<RowValue> rows) {
        this.rows = rows;
    }

    public List<RowValue> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rows", rows != null ? rows.size() : null)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return Objects.equals(rows, that.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }
}
