package de.magicline.racoon.domain.status.dto;

import java.beans.ConstructorProperties;
import java.util.Objects;

import com.google.common.base.MoreObjects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ValidationStatusDto implements ValidationStatus {

    private final String type;
    private final int code;
    private final boolean retry;
    private final String description;

    public ValidationStatusDto(ValidationStatus status) {
        this(status.getType(), status.getCode(), status.isRetry(), status.getDescription());
    }

    @ConstructorProperties({"type", "code", "retry", "description"})
    public ValidationStatusDto(String type, int code, boolean retry, String description) {
        this.type = type;
        this.code = code;
        this.retry = retry;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public boolean isRetry() {
        return retry;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationStatusDto that = (ValidationStatusDto) o;
        return getCode() == that.getCode() &&
                isRetry() == that.isRetry() &&
                getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getCode(), isRetry());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("code", code)
                .add("retry", retry)
                .add("description", description)
                .toString();
    }
}
