package de.magicline.racoon.service.rtev;

import java.beans.ConstructorProperties;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class RowValue {

    private final String email;
    private final int result;
    private final String message;

    @ConstructorProperties({"email", "result", "message"})
    public RowValue(String email, int result, String message) {
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

    public String getEmail() {
        return email;
    }

    public int getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RowValue rowDto = (RowValue) o;
        return Objects.equals(result, rowDto.result) &&
                Objects.equals(email, rowDto.email) &&
                Objects.equals(message, rowDto.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, result, message);
    }
}
