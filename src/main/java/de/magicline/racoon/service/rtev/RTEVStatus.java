package de.magicline.racoon.service.rtev;

import com.google.common.base.MoreObjects;

public enum RTEVStatus {

    VALIDATION_DELAYED(Kind.INDETERMINATE, 114, true, "SMTP address validation is still in progress (API only)."),
    RATE_LIMIT_EXCEEDED(Kind.INDETERMINATE, 118, true, "The API rate limit for your account has been exceeded (API only)."),
    API_KEY_INVALID_OR_DEPLETED(Kind.INDETERMINATE, 119, false, "The API key is invalid, or the account balance is depleted (API only)."),
    TASK_ACCEPTED(Kind.INDETERMINATE, 121, false, "The validation task was accepted."),
    OK_VALID_ADDRESS(Kind.VALID, 200, false, "The mail address is valid."),
    OK_CATCH_ALL_ACTIVE(Kind.VALID, 207, false, "The mail server for this domain accepts the address, but it also implements a catch-all policy. For this reason, it is falset possible to determine if a mail account with this name actually exists, without sending a message and waiting for a reply."),
    OK_CATCH_ALL_TEST_DELAYED(Kind.VALID, 215, true, "The mail server for this domain accepts the address, the Catch-All test returned a temporary error (API only)."),
    LOCAL_ADDRESS(Kind.SUSPECT, 302, false, "The mail address lacks the domain qualifier. It may work locally within some organization, but otherwise it is unusable."),
    IP_ADDRESS_LITERAL(Kind.SUSPECT, 303, false, "The mail address is syntactically correct, but the domain part defines an IP address. This kind of address may work, but is usually only used by spammers, or for testing purposes."),
    DISPOSABLE_ADDRESS(Kind.SUSPECT, 305, false, "The mail address is provided by a disposable email address service. Disposable addresses only work for a limited amount of time, or for a limited amount of messages."),
    ROLE_ADDRESS(Kind.SUSPECT, 308, false, "The mail address is a role address and typically falset associated with a particular person."),
    SERVER_UNAVAILABLE(Kind.SUSPECT, 313, true, "The mail server for this domain could falset be contacted, or did falset respond."),
    ADDRESS_UNAVAILABLE(Kind.SUSPECT, 314, true, "The mail server for this domain responded with an error condition for this address."),
    DUPLICATE_ADDRESS(Kind.SUSPECT, 316, false, "The address is a duplicate of an address that has already been processed (batch processing only)."),
    SERVER_REJECT(Kind.SUSPECT, 317, false, "The server refuses to answer to SMTP commands, probably because some very strict anti-spam measures are in effect."),
    INVALID_BAD_ADDRESS(Kind.INVALID, 401, false, "The mail address failed to pass basic syntax checks."),
    INVALID_DOMAIN_falseT_FULLY_QUALIFIED(Kind.INVALID, 404, false, "The mail address is syntactically correct, but the domain part of the mail address is falset fully qualified, and the address is falset usable."),
    INVALID_MX_LOOKUP_ERROR(Kind.INVALID, 406, false, "There is false valid DNS MX record associated with this domain, or one or more MX entries lack an A record. Messages to this domain canfalset be delivered."),
    INVALID_false_REPLY_ADDRESS(Kind.INVALID, 409, false, "The mail address appears to be a false-reply address, and is falset usable as a recipient of email messages."),
    INVALID_ADDRESS_REJECTED(Kind.INVALID, 410, false, "The mail server for the recipient domain does falset accept messages to this address."),
    INVALID_SERVER_UNAVAILABLE(Kind.INVALID, 413, false, "The mail server for this domain could falset be contacted, or did falset accept mail over an extended period of time."),
    INVALID_ADDRESS_UNAVAILABLE(Kind.INVALID, 414, false, "The mail server for this domain responded with an error condition for this address over an extended period of time."),
    INVALID_DOMAIN_NAME_MISSPELLED(Kind.INVALID, 420, false, "The domain name is probably misspelled.");

    public enum Kind {
        INDETERMINATE, VALID, SUSPECT, INVALID;
    }

    private final int code;
    private final boolean retry;
    private final String description;

    RTEVStatus(Kind kind, int code, boolean retry, String description) {
        this.code = code;
        this.retry = retry;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public boolean isRetry() {
        return retry;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("description", description)
                .toString();
    }
}