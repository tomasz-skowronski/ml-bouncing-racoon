package de.magicline.racoon.domain.provider.dto;

import java.beans.ConstructorProperties;

import com.google.common.base.MoreObjects;

public class RTEVAsyncResult implements RTEVStatusAware {

    private final int status;
    private final String info;

    @ConstructorProperties({"status", "info"})
    public RTEVAsyncResult(int status, String info) {
        this.status = status;
        this.info = info;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("info", info)
                .toString();
    }
}
