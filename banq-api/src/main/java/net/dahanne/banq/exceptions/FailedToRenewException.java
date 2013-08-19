package net.dahanne.banq.exceptions;

/**
 * Created by anthony on 13-08-18.
 */
public class FailedToRenewException extends Exception {

    public FailedToRenewException(String detailMessage) {
        super(detailMessage);
    }
}
