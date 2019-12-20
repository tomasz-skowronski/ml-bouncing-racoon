package de.magicline.racoon.domain.provider.dto;

import java.beans.ConstructorProperties;

import com.google.common.base.MoreObjects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class RTEVResult implements RTEVStatusAware {

    private final int status;
    private final String info;
    private final String details;
    private final Integer ratelimitRemain;
    private final Integer ratelimitSeconds;

    public RTEVResult(RTEVValidationStatus status) {
        this(status.getCode(), status.name(), status.getDescription(), null, null);
    }

    @ConstructorProperties({"status", "info", "details", "ratelimitRemain", "ratelimitSeconds"})
    public RTEVResult(int status, String info, String details, Integer ratelimitRemain, Integer ratelimitSeconds) {
        this.status = status;
        this.info = info;
        this.details = details;
        this.ratelimitRemain = ratelimitRemain;
        this.ratelimitSeconds = ratelimitSeconds;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    public String getDetails() {
        return details;
    }

    /**
     * @return number of API requests remaining before the API rate limit is reached
     * (the default API rate limit allows 100 API requests in 300s)
     */
    public Integer getRatelimitRemain() {
        return ratelimitRemain;
    }

    /**
     * @return number of seconds remaining in the current rate limit interval
     */
    public Integer getRatelimitSeconds() {
        return ratelimitSeconds;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("info", info)
                .add("details", details)
                .toString();
    }
}
