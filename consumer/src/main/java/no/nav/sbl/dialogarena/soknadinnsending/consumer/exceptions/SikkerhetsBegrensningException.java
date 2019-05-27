package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;


import no.nav.sbl.dialogarena.sendsoknad.domain.exception.ApplicationException;

public class SikkerhetsBegrensningException extends ApplicationException {
    public SikkerhetsBegrensningException(String message, Exception exception) {
        super(message, exception);
    }
}
