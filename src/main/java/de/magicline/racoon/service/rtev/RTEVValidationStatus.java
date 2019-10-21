package de.magicline.racoon.service.rtev;

import de.magicline.racoon.service.status.ValidationStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * https://www.email-validator.net/results.html
 */
public enum RTEVValidationStatus implements ValidationStatus {

    VALIDATION_DELAYED(Type.INDETERMINATE, 114, true,
            "SMTP address validation is still in progress (API only)."),
    RATE_LIMIT_EXCEEDED(Type.INDETERMINATE, 118, true,
            "The API rate limit for your account has been exceeded (API only)."),
    API_KEY_INVALID_OR_DEPLETED(Type.INDETERMINATE, 119, false,
            "The API key is invalid, or the account balance is depleted (API only)."),
    TASK_ACCEPTED(Type.INDETERMINATE, 121, false,
            "The validation task was accepted."),
    OK_VALID_ADDRESS(Type.VALID, 200, false,
            "The mail address is valid."),
    OK_CATCH_ALL_ACTIVE(Type.VALID, 207, false,
            "The mail server for this domain accepts the address, but it also implements a catch-all policy. " +
                    "For this reason, it is not possible to determine if a mail account with this name actually exists, " +
                    "without sending a message and waiting for a reply."),
    OK_CATCH_ALL_TEST_DELAYED(Type.VALID, 215, true,
            "The mail server for this domain accepts the address, the Catch-All test returned a temporary error (API only)."),
    LOCAL_ADDRESS(Type.SUSPECT, 302, false,
            "The mail address lacks the domain qualifier. It may work locally within some organization, but otherwise it is unusable."),
    IP_ADDRESS_LITERAL(Type.SUSPECT, 303, false,
            "The mail address is syntactically correct, but the domain part defines an IP address. " +
                    "This kind of address may work, but is usually only used by spammers, or for testing purposes."),
    DISPOSABLE_ADDRESS(Type.SUSPECT, 305, false,
            "The mail address is provided by a disposable email address service. " +
                    "Disposable addresses only work for a limited amount of time, or for a limited amount of messages."),
    ROLE_ADDRESS(Type.SUSPECT, 308, false,
            "The mail address is a role address and typically not associated with a particular person."),
    SERVER_UNAVAILABLE(Type.SUSPECT, 313, true,
            "The mail server for this domain could not be contacted, or did not respond."),
    ADDRESS_UNAVAILABLE(Type.SUSPECT, 314, true,
            "The mail server for this domain responded with an error condition for this address."),
    DUPLICATE_ADDRESS(Type.SUSPECT, 316, false,
            "The address is a duplicate of an address that has already been processed (batch processing only)."),
    SERVER_REJECT(Type.SUSPECT, 317, false,
            "The server refuses to answer to SMTP commands, probably because some very strict anti-spam measures are in effect."),
    INVALID_BAD_ADDRESS(Type.INVALID, 401, false,
            "The mail address failed to pass basic syntax checks."),
    INVALID_DOMAIN_NOT_FULLY_QUALIFIED(Type.INVALID, 404, false,
            "The mail address is syntactically correct, but the domain part of the mail address is not fully qualified, " +
                    "and the address is not usable."),
    INVALID_MX_LOOKUP_ERROR(Type.INVALID, 406, false,
            "There is no valid DNS MX record associated with this domain, or one or more MX entries lack an A record. " +
                    "Messages to this domain cannot be delivered."),
    INVALID_NO_REPLY_ADDRESS(Type.INVALID, 409, false,
            "The mail address appears to be a no-reply address, and is not usable as a recipient of email messages."),
    INVALID_ADDRESS_REJECTED(Type.INVALID, 410, false,
            "The mail server for the recipient domain does not accept messages to this address."),
    INVALID_SERVER_UNAVAILABLE(Type.INVALID, 413, false,
            "The mail server for this domain could not be contacted, or did not accept mail over an extended period of time."),
    INVALID_ADDRESS_UNAVAILABLE(Type.INVALID, 414, false,
            "The mail server for this domain responded with an error condition for this address over an extended period of time."),
    INVALID_DOMAIN_NAME_MISSPELLED(Type.INVALID, 420, false,
            "The domain name is probably misspelled.");

    public enum Type {
        INDETERMINATE, VALID, SUSPECT, INVALID;
    }

    private static final Map<Integer, RTEVValidationStatus> MAPPING_BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(
                    RTEVValidationStatus::getCode,
                    Function.identity()));

    private final Type type;
    private final int code;
    private final boolean retry;
    private final String description;

    RTEVValidationStatus(Type type, int code, boolean retry, String description) {
        this.type = type;
        this.code = code;
        this.retry = retry;
        this.description = description;
    }

    public static RTEVValidationStatus of(int result) {
        return MAPPING_BY_ID.get(result);
    }

    @Override
    public String getType() {
        return type.name();
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
}
