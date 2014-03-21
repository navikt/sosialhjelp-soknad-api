package no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception;

import no.nav.modig.core.exception.ApplicationException;


public class SoknadAvsluttetException extends ApplicationException {
    public SoknadAvsluttetException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }
}
