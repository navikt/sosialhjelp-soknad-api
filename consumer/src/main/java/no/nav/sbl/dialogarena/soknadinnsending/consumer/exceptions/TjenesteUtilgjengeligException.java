package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.modig.core.exception.ApplicationException;

public class TjenesteUtilgjengeligException extends ApplicationException {
    public TjenesteUtilgjengeligException(String message, Exception exception) {
        super(message, exception);
    }
}
