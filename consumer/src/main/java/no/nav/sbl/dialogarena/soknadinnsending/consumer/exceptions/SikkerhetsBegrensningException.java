package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class SikkerhetsBegrensningException extends SosialhjelpSoknadApiException {
    public SikkerhetsBegrensningException(String message, Exception exception) {
        super(message, exception);
    }
}
