package no.nav.sosialhjelp.soknad.consumer.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SikkerhetsBegrensningException extends SosialhjelpSoknadApiException {
    public SikkerhetsBegrensningException(String message, Exception exception) {
        super(message, exception);
    }
}
