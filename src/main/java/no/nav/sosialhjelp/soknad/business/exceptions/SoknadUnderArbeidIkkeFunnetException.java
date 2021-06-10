package no.nav.sosialhjelp.soknad.business.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SoknadUnderArbeidIkkeFunnetException extends SosialhjelpSoknadApiException {
    public SoknadUnderArbeidIkkeFunnetException(String message) {
        super(message);
    }
}
