package de.magicline.racoon.service.status;

import java.beans.ConstructorProperties;
import java.util.Objects;

import com.google.common.base.MoreObjects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class StatusItem {

    private final String email;

    @ConstructorProperties({"email"})
    public StatusItem(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusItem that = (StatusItem) o;
        return getEmail().equals(that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("email", email)
                .toString();
    }
}
