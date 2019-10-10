package de.magicline.racoon.service.status;

import java.beans.ConstructorProperties;
import java.util.Objects;

import com.google.common.base.MoreObjects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class StatusItem {

    private String email;
    private int code;

    @ConstructorProperties({"email", "code"})
    public StatusItem(String email, int code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusItem that = (StatusItem) o;
        return code == that.code &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, code);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("email", email)
                .add("code", code)
                .toString();
    }
}
