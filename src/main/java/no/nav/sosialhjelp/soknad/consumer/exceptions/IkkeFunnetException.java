package no.nav.sosialhjelp.soknad.consumer.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;


public class IkkeFunnetException extends SosialhjelpSoknadApiException {
    public IkkeFunnetException(String melding, Exception e) {
        super(melding, e);
    }
}
