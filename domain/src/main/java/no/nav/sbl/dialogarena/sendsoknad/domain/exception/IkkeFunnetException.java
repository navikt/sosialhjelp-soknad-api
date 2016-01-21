package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

import no.nav.modig.core.exception.ApplicationException;


public class IkkeFunnetException extends ApplicationException {

    public IkkeFunnetException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }

}
