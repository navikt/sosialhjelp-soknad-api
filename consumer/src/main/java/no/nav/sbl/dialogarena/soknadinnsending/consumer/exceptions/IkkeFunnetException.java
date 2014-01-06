package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.modig.core.exception.ApplicationException;


public class IkkeFunnetException extends ApplicationException {
    public IkkeFunnetException(String melding, Exception e) {
        super(melding, e);
    }
}
