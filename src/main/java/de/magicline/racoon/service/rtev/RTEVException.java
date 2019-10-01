package de.magicline.racoon.service.rtev;

import com.google.common.base.MoreObjects;

public class RTEVException extends RuntimeException {

    public enum Error {
        RESPONSE_REDIRECT,
        RESPONSE_CONTENT_TYPE,
        RESPONSE_CONTENT
    }

    private final Error error;

    public RTEVException(Error error, String message) {
        super(message);
        this.error = error;
    }

    public RTEVException(Error error, Exception cause) {
        super(cause);
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("error", error)
                .addValue(MoreObjects.firstNonNull(getCause(), getMessage()))
                .toString();
    }
}
