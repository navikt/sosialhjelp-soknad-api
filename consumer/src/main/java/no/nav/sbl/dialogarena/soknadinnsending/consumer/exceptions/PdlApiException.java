package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class PdlApiException extends SosialhjelpSoknadApiException {

    public PdlApiException(String message) {
        super(message);
    }
}
