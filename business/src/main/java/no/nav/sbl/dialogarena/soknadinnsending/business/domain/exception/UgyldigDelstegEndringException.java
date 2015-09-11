package no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception;

import no.nav.modig.core.exception.ApplicationException;


public class UgyldigDelstegEndringException extends ApplicationException {

    public UgyldigDelstegEndringException(String message, String id) {
        super(message, null, id);
    }

}
