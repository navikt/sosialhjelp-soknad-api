package no.nav.sbl.dialogarena.common;

import no.nav.modig.core.exception.ApplicationException;

public class UnableToHandleException extends ApplicationException {

    public UnableToHandleException(Object something) {
        super("I don't know how to handle " + something);
    }

    public UnableToHandleException(Object something, Throwable nested) {
        super("I don't know how to handle " + something + ". See nested exception for additional information ("
            + nested.getMessage() + ")", nested);
    }
}
