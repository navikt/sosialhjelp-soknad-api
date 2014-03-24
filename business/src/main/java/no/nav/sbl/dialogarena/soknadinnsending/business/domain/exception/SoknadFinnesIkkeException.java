package no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception;

import no.nav.modig.core.exception.ApplicationException;


public class SoknadFinnesIkkeException extends ApplicationException {
    public SoknadFinnesIkkeException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }
}
