package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;


public class IkkeFunnetException extends SosialhjelpSoknadApiException {
    public IkkeFunnetException(String melding, Exception e) {
        super(melding, e);
    }
}
