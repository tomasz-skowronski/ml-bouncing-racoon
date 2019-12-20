package de.magicline.racoon.domain.provider.dto;

import de.magicline.racoon.domain.task.dto.RowValue;

import java.beans.ConstructorProperties;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class RTEVRowValue implements RowValue {

    private final String email;
    private final int result;
    private final String message;

    @ConstructorProperties({"email", "result", "message"})
    public RTEVRowValue(String email, int result, String message) {
        this.email = email;
        this.result = result;
        this.message = message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("email", email)
                .add("result", result)
                .add("message", message)
                .toString();
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public int getResult() {
        return result;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RTEVRowValue rowDto = (RTEVRowValue) o;
        return Objects.equals(result, rowDto.result) &&
                Objects.equals(email, rowDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, result);
    }
}
