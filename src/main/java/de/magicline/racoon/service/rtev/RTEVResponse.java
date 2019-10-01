package de.magicline.racoon.service.rtev;

import com.google.common.base.MoreObjects;

public class RTEVResponse {

    private int status;
    private String info;
    private String details;
    private Integer ratelimitRemain;
    private Integer ratelimitSeconds;
    private Boolean freemail;

    public RTEVResponse() {
        super();
    }

    public RTEVResponse(RTEVStatus status) {
        this(status.getCode(), status.name(), status.getDescription());
    }

    public RTEVResponse(int status, String info, String details) {
        this.status = status;
        this.info = info;
        this.details = details;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getRatelimitRemain() {
        return ratelimitRemain;
    }

    public void setRatelimitRemain(Integer ratelimitRemain) {
        this.ratelimitRemain = ratelimitRemain;
    }

    public Integer getRatelimitSeconds() {
        return ratelimitSeconds;
    }

    public void setRatelimitSeconds(Integer ratelimitSeconds) {
        this.ratelimitSeconds = ratelimitSeconds;
    }

    public Boolean isFreemail() {
        return freemail;
    }

    public void setFreemail(Boolean freemail) {
        this.freemail = freemail;
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
