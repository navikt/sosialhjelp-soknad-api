package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class PdlApiException extends SosialhjelpSoknadApiException {

    public PdlApiException(String message) {
        super(message);
    }

    public PdlApiException(String message, Throwable t) {
        super(message, t);
    }
}
